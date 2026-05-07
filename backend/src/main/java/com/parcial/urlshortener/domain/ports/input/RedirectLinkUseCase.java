package com.parcial.urlshortener.domain.ports.input;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface RedirectLinkUseCase {
    CompletableFuture<Optional<String>> resolveOriginalUrl(String shortCode);
}
