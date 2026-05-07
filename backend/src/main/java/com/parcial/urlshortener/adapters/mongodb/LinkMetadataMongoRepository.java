package com.parcial.urlshortener.adapters.mongodb;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LinkMetadataMongoRepository extends MongoRepository<LinkMetadataDocument, String> {
    Optional<LinkMetadataDocument> findByShortCode(String shortCode);
}
