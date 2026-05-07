package com.parcial.urlshortener.domain.ports.input;

import com.parcial.urlshortener.domain.model.Link;
import com.parcial.urlshortener.domain.model.LinkMetadata;

import java.util.concurrent.CompletableFuture;

public interface ShortenLinkUseCase {
    CompletableFuture<Link> shortenLink(String originalUrl, String imageUrl, String description);
}
