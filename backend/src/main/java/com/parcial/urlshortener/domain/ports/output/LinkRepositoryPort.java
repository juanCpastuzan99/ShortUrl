package com.parcial.urlshortener.domain.ports.output;

import com.parcial.urlshortener.domain.model.Link;

import java.util.List;
import java.util.Optional;

public interface LinkRepositoryPort {
    Link save(Link link);
    Optional<Link> findByShortCode(String shortCode);
    List<Link> findAll();
    void incrementClickCount(String shortCode);
}
