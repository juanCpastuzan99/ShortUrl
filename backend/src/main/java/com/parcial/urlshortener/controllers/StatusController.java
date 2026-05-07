package com.parcial.urlshortener.controllers;

import com.parcial.urlshortener.domain.ports.output.LinkMetadataRepositoryPort;
import com.parcial.urlshortener.domain.ports.output.LinkRepositoryPort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/status")
@CrossOrigin(origins = "*")
public class StatusController {

    private final LinkRepositoryPort linkRepository;
    private final LinkMetadataRepositoryPort metadataRepository;
    private final StringRedisTemplate redisTemplate;

    public StatusController(LinkRepositoryPort linkRepository,
                            LinkMetadataRepositoryPort metadataRepository,
                            StringRedisTemplate redisTemplate) {
        this.linkRepository = linkRepository;
        this.metadataRepository = metadataRepository;
        this.redisTemplate = redisTemplate;
    }

    @GetMapping
    public Map<String, Object> status() {
        Map<String, Object> response = new HashMap<>();
        response.put("mysql", checkMysql());
        response.put("mongo", checkMongo());
        response.put("redis", checkRedis());
        response.put("threads", Map.of(
                "active", Thread.activeCount(),
                "name", Thread.currentThread().getName()
        ));
        return response;
    }

    private Map<String, Object> checkMysql() {
        Map<String, Object> info = new HashMap<>();
        try {
            int count = linkRepository.findAll().size();
            info.put("up", true);
            info.put("count", count);
        } catch (Exception ex) {
            info.put("up", false);
            info.put("error", ex.getClass().getSimpleName());
        }
        return info;
    }

    private Map<String, Object> checkMongo() {
        Map<String, Object> info = new HashMap<>();
        try {
            int count = metadataRepository.findAll().size();
            info.put("up", true);
            info.put("count", count);
        } catch (Exception ex) {
            info.put("up", false);
            info.put("error", ex.getClass().getSimpleName());
        }
        return info;
    }

    private Map<String, Object> checkRedis() {
        Map<String, Object> info = new HashMap<>();
        try {
            Set<String> keys = redisTemplate.keys("link:*");
            info.put("up", true);
            info.put("count", keys != null ? keys.size() : 0);
        } catch (Exception ex) {
            info.put("up", false);
            info.put("error", ex.getClass().getSimpleName());
        }
        return info;
    }
}
