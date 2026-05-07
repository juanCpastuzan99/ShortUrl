package com.parcial.urlshortener.infrastructure.adapters.output.mongodb;

import com.parcial.urlshortener.domain.model.LinkMetadata;
import com.parcial.urlshortener.domain.ports.output.LinkMetadataRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MongoLinkMetadataRepositoryAdapter implements LinkMetadataRepositoryPort {

    private final LinkMetadataMongoRepository mongoRepository;

    public MongoLinkMetadataRepositoryAdapter(LinkMetadataMongoRepository mongoRepository) {
        this.mongoRepository = mongoRepository;
    }

    @Override
    public LinkMetadata save(LinkMetadata metadata) {
        LinkMetadataDocument doc = toDocument(metadata);
        LinkMetadataDocument saved = mongoRepository.save(doc);
        return toDomain(saved);
    }

    @Override
    public Optional<LinkMetadata> findByShortCode(String shortCode) {
        return mongoRepository.findByShortCode(shortCode).map(this::toDomain);
    }

    @Override
    public List<LinkMetadata> findAll() {
        return mongoRepository.findAll()
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private LinkMetadataDocument toDocument(LinkMetadata metadata) {
        LinkMetadataDocument doc = new LinkMetadataDocument();
        doc.setId(metadata.getId());
        doc.setShortCode(metadata.getShortCode());
        doc.setImageUrl(metadata.getImageUrl());
        doc.setDescription(metadata.getDescription());
        return doc;
    }

    private LinkMetadata toDomain(LinkMetadataDocument doc) {
        LinkMetadata metadata = new LinkMetadata();
        metadata.setId(doc.getId());
        metadata.setShortCode(doc.getShortCode());
        metadata.setImageUrl(doc.getImageUrl());
        metadata.setDescription(doc.getDescription());
        return metadata;
    }
}
