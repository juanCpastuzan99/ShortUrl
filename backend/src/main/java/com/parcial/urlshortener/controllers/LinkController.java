package com.parcial.urlshortener.controllers;

import com.parcial.urlshortener.domain.model.Link;
import com.parcial.urlshortener.domain.model.LinkReport;
import com.parcial.urlshortener.domain.ports.input.GetLinksUseCase;
import com.parcial.urlshortener.domain.ports.input.ShortenLinkUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/links")
@CrossOrigin(origins = "*")
public class LinkController {

    private final ShortenLinkUseCase shortenLinkUseCase;
    private final GetLinksUseCase getLinksUseCase;

    public LinkController(ShortenLinkUseCase shortenLinkUseCase, GetLinksUseCase getLinksUseCase) {
        this.shortenLinkUseCase = shortenLinkUseCase;
        this.getLinksUseCase = getLinksUseCase;
    }

    @PostMapping
    public CompletableFuture<ResponseEntity<LinkResponse>> shortenLink(@Valid @RequestBody ShortenRequest request) {
        return shortenLinkUseCase
                .shortenLink(request.originalUrl(), request.imageUrl(), request.description())
                .thenApply(link -> ResponseEntity.ok(new LinkResponse(
                        link.getShortCode(),
                        link.getOriginalUrl(),
                        link.getShortUrl()
                )));
    }

    @GetMapping
    public CompletableFuture<ResponseEntity<List<LinkReport>>> getAllLinks() {
        return getLinksUseCase.getAllLinks()
                .thenApply(ResponseEntity::ok);
    }

    public record ShortenRequest(
            @NotBlank(message = "El enlace original es obligatorio")
            @Pattern(
                    regexp = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$",
                    message = "Debe ser una URL válida (http:// o https://)"
            )
            String originalUrl,

            @NotBlank(message = "El enlace de la imagen es obligatorio")
            @Pattern(
                    regexp = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$",
                    message = "La imagen debe ser una URL válida"
            )
            String imageUrl,

            @NotBlank(message = "La descripción es obligatoria")
            @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
            @Pattern(
                    regexp = "(?s)^\\s*\\S+(\\s+\\S+){4,}\\s*$",
                    message = "La descripción debe tener al menos 5 palabras"
            )
            String description
    ) {}

    public record LinkResponse(
            String shortCode,
            String originalUrl,
            String shortUrl
    ) {}
}
