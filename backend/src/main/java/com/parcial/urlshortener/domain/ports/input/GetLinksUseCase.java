package com.parcial.urlshortener.domain.ports.input;

import com.parcial.urlshortener.domain.model.LinkReport;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface GetLinksUseCase {
    CompletableFuture<List<LinkReport>> getAllLinks();
}
