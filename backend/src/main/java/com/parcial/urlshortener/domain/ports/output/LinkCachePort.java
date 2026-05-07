package com.parcial.urlshortener.domain.ports.output;

import java.util.Optional;

public interface LinkCachePort {
    void cacheLink(String shortCode, String originalUrl);
    Optional<String> getCachedLink(String shortCode);
    boolean isCacheable(String originalUrl);
}
