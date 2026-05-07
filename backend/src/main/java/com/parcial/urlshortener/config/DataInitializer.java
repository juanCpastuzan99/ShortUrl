package com.parcial.urlshortener.config;

import com.parcial.urlshortener.domain.model.LinkMetadata;
import com.parcial.urlshortener.domain.ports.output.LinkCachePort;
import com.parcial.urlshortener.domain.ports.output.LinkMetadataRepositoryPort;
import com.parcial.urlshortener.domain.ports.output.LinkRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Auto-carga datos de ejemplo en MongoDB y Redis al arrancar la aplicación.
 * Solo inserta si las colecciones/cachés están vacías (idempotente).
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final LinkMetadataRepositoryPort metadataRepository;
    private final LinkCachePort cachePort;
    private final LinkRepositoryPort linkRepository;

    public DataInitializer(LinkMetadataRepositoryPort metadataRepository,
                           LinkCachePort cachePort,
                           LinkRepositoryPort linkRepository) {
        this.metadataRepository = metadataRepository;
        this.cachePort = cachePort;
        this.linkRepository = linkRepository;
    }

    @Override
    public void run(String... args) {
        try { seedMongo(); } catch (Exception e) { log.warn("[INIT] MongoDB seed falló: {}", e.getMessage()); }
        try { seedRedis(); } catch (Exception e) { log.warn("[INIT] Redis seed falló (¿Memurai apagado?): {}", e.getMessage()); }
    }

    private void seedMongo() {
        if (!metadataRepository.findAll().isEmpty()) {
            log.info("[INIT] MongoDB ya tiene datos, no se inserta seed");
            return;
        }

        List<LinkMetadata> seed = List.of(
            meta("4c92ab", "https://upload.wikimedia.org/wikipedia/commons/thumb/8/80/Wikipedia-logo-v2.svg/200px-Wikipedia-logo-v2.svg.png",
                "Articulo sobre arquitectura hexagonal en Wikipedia explicando sus principios fundamentales"),
            meta("5d83bc", "https://spring.io/img/spring-2.svg",
                "Sitio oficial de Spring Boot framework Java mas popular para crear aplicaciones de produccion"),
            meta("6e74cd", "https://www.mongodb.com/assets/images/global/leaf.png",
                "Documentacion oficial de MongoDB la base de datos NoSQL orientada a documentos mas usada del mundo"),
            meta("7f65de", "https://redis.io/wp-content/uploads/2024/04/Logotype.svg",
                "Guia de inicio rapido de Redis sistema de cache en memoria de alto rendimiento y baja latencia"),
            meta("8a56ef", "https://labs.mysql.com/common/logos/mysql-logo.svg",
                "Introduccion oficial a MySQL el sistema de gestion de bases de datos relacionales mas popular open source"),
            meta("9b47f0", "https://www.docker.com/wp-content/uploads/2022/03/Moby-logo.png",
                "Introduccion a Docker sistema de contenedores que facilita el despliegue de aplicaciones en cualquier entorno"),
            meta("0c38a1", "https://www.youtube.com/img/desktop/yt_1200.png",
                "Video viral de YouTube que se ha convertido en un clasico de la cultura de internet muy conocido")
        );

        seed.forEach(metadataRepository::save);
        log.info("[INIT] MongoDB inicializado con {} documentos", seed.size());
    }

    private void seedRedis() {
        // Cachear solo URLs con longitud >= 50 caracteres
        linkRepository.findAll().forEach(link -> {
            if (link.getOriginalUrl().length() >= 50) {
                cachePort.cacheLink(link.getShortCode(), link.getOriginalUrl());
            }
        });
        log.info("[INIT] Redis inicializado con URLs largas (>=50 chars)");
    }

    private LinkMetadata meta(String shortCode, String imageUrl, String description) {
        return new LinkMetadata(shortCode, imageUrl, description);
    }
}
