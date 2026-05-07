package com.parcial.urlshortener.infrastructure.adapters.input.rest;

import com.parcial.urlshortener.domain.ports.input.RedirectLinkUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

@RestController
@CrossOrigin(origins = "*")
public class RedirectController {

    private final RedirectLinkUseCase redirectLinkUseCase;

    public RedirectController(RedirectLinkUseCase redirectLinkUseCase) {
        this.redirectLinkUseCase = redirectLinkUseCase;
    }

    @GetMapping("/r/{shortCode}")
    public CompletableFuture<ResponseEntity<Void>> redirect(@PathVariable String shortCode) {
        return redirectLinkUseCase.resolveOriginalUrl(shortCode)
                .thenApply(originalUrl -> originalUrl
                        .map(url -> ResponseEntity.status(HttpStatus.FOUND)
                                .location(URI.create(url))
                                .<Void>build())
                        .orElse(ResponseEntity.notFound().build()));
    }
}
