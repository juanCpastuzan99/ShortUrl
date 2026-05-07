package com.parcial.urlshortener.application.service;

import com.parcial.urlshortener.domain.model.Link;
import com.parcial.urlshortener.domain.model.LinkMetadata;
import com.parcial.urlshortener.domain.model.LinkReport;
import com.parcial.urlshortener.domain.ports.input.GetLinksUseCase;
import com.parcial.urlshortener.domain.ports.input.RedirectLinkUseCase;
import com.parcial.urlshortener.domain.ports.input.ShortenLinkUseCase;
import com.parcial.urlshortener.domain.ports.output.LinkCachePort;
import com.parcial.urlshortener.domain.ports.output.LinkMetadataRepositoryPort;
import com.parcial.urlshortener.domain.ports.output.LinkRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * ============================================================================
 *  CONCURRENCIA APLICADA EN ESTE SERVICIO
 * ============================================================================
 *  1. @Async("taskExecutor")  → ejecuta los casos de uso en hilos del pool
 *  2. CompletableFuture       → composición asíncrona de tareas paralelas
 *  3. ThreadPoolTaskExecutor  → pool de 10-50 hilos (ver AsyncConfig)
 *  4. ReentrantReadWriteLock  → permite múltiples lectores concurrentes
 *                               y escritores exclusivos
 *  5. AtomicLong              → generación thread-safe de códigos cortos
 *  6. ConcurrentHashMap       → índice en memoria sin bloqueos
 *  7. Semaphore               → limita escrituras simultáneas (rate-limit)
 *  8. allOf() / supplyAsync() → escrituras a las 3 BD en PARALELO
 * ============================================================================
 */
@Service
public class LinkService implements ShortenLinkUseCase, GetLinksUseCase, RedirectLinkUseCase {

    private static final Logger log = LoggerFactory.getLogger(LinkService.class);
    private static final String BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private final LinkRepositoryPort linkRepository;
    private final LinkMetadataRepositoryPort metadataRepository;
    private final LinkCachePort cachePort;
    private final Executor taskExecutor;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.redis-cache-min-length:50}")
    private int redisCacheMinLength;

    // ═══════ Mecanismos de concurrencia ═══════
    private final AtomicLong counter = new AtomicLong(System.currentTimeMillis() % 1_000_000L);
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true); // fair lock
    private final Map<String, String> shortCodeIndex = new ConcurrentHashMap<>();
    private final Semaphore writeSemaphore = new Semaphore(20); // max 20 escrituras concurrentes

    public LinkService(LinkRepositoryPort linkRepository,
                       LinkMetadataRepositoryPort metadataRepository,
                       LinkCachePort cachePort,
                       @Qualifier("taskExecutor") Executor taskExecutor) {
        this.linkRepository = linkRepository;
        this.metadataRepository = metadataRepository;
        this.cachePort = cachePort;
        this.taskExecutor = taskExecutor;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //   CASO DE USO 1: ACORTAR ENLACE (escribe en MySQL + Mongo + Redis EN PARALELO)
    // ═════════════════════════════════════════════════════════════════════════
    @Override
    @Async("taskExecutor")
    public CompletableFuture<Link> shortenLink(String originalUrl, String imageUrl, String description) {
        log.info("[CONCURRENT] shortenLink iniciado en hilo: {}", Thread.currentThread().getName());

        try {
            writeSemaphore.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return CompletableFuture.failedFuture(e);
        }

        lock.writeLock().lock();
        try {
            String shortCode = generateShortCode(counter.getAndIncrement());
            Link link = new Link(shortCode, originalUrl, buildShortUrl(shortCode));

            // ── Las 3 escrituras se lanzan EN PARALELO usando CompletableFuture ──
            CompletableFuture<Link> mysqlFuture = CompletableFuture.supplyAsync(() -> {
                log.info("  [MySQL] guardando en hilo: {}", Thread.currentThread().getName());
                return linkRepository.save(link);
            }, taskExecutor);

            CompletableFuture<LinkMetadata> mongoFuture = CompletableFuture.supplyAsync(() -> {
                log.info("  [Mongo] guardando en hilo: {}", Thread.currentThread().getName());
                return metadataRepository.save(new LinkMetadata(shortCode, imageUrl, description));
            }, taskExecutor);

            CompletableFuture<Void> redisFuture = CompletableFuture.runAsync(() -> {
                log.info("  [Redis] verificando cache en hilo: {}", Thread.currentThread().getName());
                if (originalUrl.length() >= redisCacheMinLength) {
                    cachePort.cacheLink(shortCode, originalUrl);
                    log.info("  [Redis] CACHEADO (URL >= {} chars)", redisCacheMinLength);
                } else {
                    log.info("  [Redis] omitido (URL < {} chars)", redisCacheMinLength);
                }
            }, taskExecutor);

            // Esperar a que las 3 terminen en paralelo
            CompletableFuture.allOf(mysqlFuture, mongoFuture, redisFuture).join();

            Link saved = mysqlFuture.join();
            saved.setShortUrl(buildShortUrl(saved.getShortCode()));
            shortCodeIndex.put(shortCode, originalUrl);

            log.info("[CONCURRENT] shortenLink COMPLETADO → {}", saved.getShortUrl());
            return CompletableFuture.completedFuture(saved);

        } finally {
            lock.writeLock().unlock();
            writeSemaphore.release();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //   CASO DE USO 2: OBTENER REPORTE (lee MySQL + Mongo EN PARALELO)
    // ═════════════════════════════════════════════════════════════════════════
    @Override
    @Async("taskExecutor")
    public CompletableFuture<List<LinkReport>> getAllLinks() {
        log.info("[CONCURRENT] getAllLinks iniciado en hilo: {}", Thread.currentThread().getName());

        lock.readLock().lock();
        try {
            // Lecturas a MySQL y Mongo EN PARALELO
            CompletableFuture<List<Link>> linksFuture = CompletableFuture.supplyAsync(() -> {
                log.info("  [MySQL] leyendo en hilo: {}", Thread.currentThread().getName());
                return linkRepository.findAll();
            }, taskExecutor);

            CompletableFuture<List<LinkMetadata>> metadataFuture = CompletableFuture.supplyAsync(() -> {
                log.info("  [Mongo] leyendo en hilo: {}", Thread.currentThread().getName());
                return metadataRepository.findAll();
            }, taskExecutor);

            // Combinar ambos resultados cuando ambos terminen
            return linksFuture.thenCombineAsync(metadataFuture, (links, metadataList) -> {
                Map<String, LinkMetadata> metadataMap = new ConcurrentHashMap<>();
                metadataList.parallelStream().forEach(m -> metadataMap.put(m.getShortCode(), m));

                List<LinkReport> reports = new ArrayList<>();
                links.parallelStream().forEachOrdered(link -> {
                    LinkReport report = new LinkReport();
                    report.setShortCode(link.getShortCode());
                    report.setOriginalUrl(link.getOriginalUrl());
                    report.setShortUrl(buildShortUrl(link.getShortCode()));
                    report.setCreatedAt(link.getCreatedAt());
                    report.setClickCount(0L);

                    LinkMetadata meta = metadataMap.get(link.getShortCode());
                    if (meta != null) {
                        report.setImageUrl(meta.getImageUrl());
                        report.setDescription(meta.getDescription());
                    }
                    synchronized (reports) {
                        reports.add(report);
                    }
                });

                log.info("[CONCURRENT] getAllLinks COMPLETADO → {} enlaces", reports.size());
                return reports;
            }, taskExecutor);

        } finally {
            lock.readLock().unlock();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //   CASO DE USO 3: REDIRIGIR (lookup ultra-rápido con cache de 3 niveles)
    // ═════════════════════════════════════════════════════════════════════════
    @Override
    @Async("taskExecutor")
    public CompletableFuture<Optional<String>> resolveOriginalUrl(String shortCode) {
        log.info("[CONCURRENT] resolveOriginalUrl({}) en hilo: {}", shortCode, Thread.currentThread().getName());

        // Nivel 1: Redis (más rápido, distribuido)
        Optional<String> cached = cachePort.getCachedLink(shortCode);
        if (cached.isPresent()) {
            log.info("  [HIT] Redis cache");
            return CompletableFuture.completedFuture(cached);
        }

        // Nivel 2: ConcurrentHashMap (memoria local, sin bloqueos)
        String inMemory = shortCodeIndex.get(shortCode);
        if (inMemory != null) {
            log.info("  [HIT] memoria local (ConcurrentHashMap)");
            return CompletableFuture.completedFuture(Optional.of(inMemory));
        }

        // Nivel 3: MySQL (último recurso, persiste en disco)
        Optional<Link> link = linkRepository.findByShortCode(shortCode);
        link.ifPresent(l -> {
            shortCodeIndex.put(shortCode, l.getOriginalUrl());
            if (l.getOriginalUrl().length() >= redisCacheMinLength) {
                // Recachear de forma asíncrona para no bloquear la respuesta
                CompletableFuture.runAsync(
                        () -> cachePort.cacheLink(shortCode, l.getOriginalUrl()),
                        taskExecutor
                );
            }
            log.info("  [HIT] MySQL");
        });

        return CompletableFuture.completedFuture(link.map(Link::getOriginalUrl));
    }

    // ═════════════════════════════════════════════════════════════════════════
    //   UTILIDADES
    // ═════════════════════════════════════════════════════════════════════════
    private String buildShortUrl(String shortCode) {
        return baseUrl + "/r/" + shortCode;
    }

    private String generateShortCode(long number) {
        StringBuilder sb = new StringBuilder();
        long n = Math.abs(number);
        if (n == 0) n = 1;
        while (n > 0) {
            sb.append(BASE62.charAt((int) (n % 62)));
            n /= 62;
        }
        while (sb.length() < 6) sb.append('0');
        return sb.reverse().toString();
    }
}
