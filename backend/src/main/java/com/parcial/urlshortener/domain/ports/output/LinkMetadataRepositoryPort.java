package com.parcial.urlshortener.domain.ports.output;

import com.parcial.urlshortener.domain.model.LinkMetadata;

import java.util.List;
import java.util.Optional;

public interface LinkMetadataRepositoryPort {
    LinkMetadata save(LinkMetadata metadata);
    Optional<LinkMetadata> findByShortCode(String shortCode);
    List<LinkMetadata> findAll();
}
