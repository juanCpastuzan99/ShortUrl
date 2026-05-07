package com.parcial.urlshortener.infrastructure.adapters.output.mysql;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * MySQL almacena ÚNICAMENTE: enlace original + enlace acortado (+ id y fecha).
 * La imagen y descripción viven en MongoDB; la caché de URLs largas en Redis.
 */
@Entity
@Table(name = "enlaces")
public class LinkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "enlaceOriginal", nullable = false, length = 2048)
    private String enlaceOriginal;

    @Column(name = "enlaceAcortado", nullable = false, length = 50, unique = true)
    private String enlaceAcortado;

    @Column(name = "creadoEn", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    public void prePersist() {
        if (creadoEn == null) creadoEn = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEnlaceOriginal() { return enlaceOriginal; }
    public void setEnlaceOriginal(String enlaceOriginal) { this.enlaceOriginal = enlaceOriginal; }

    public String getEnlaceAcortado() { return enlaceAcortado; }
    public void setEnlaceAcortado(String enlaceAcortado) { this.enlaceAcortado = enlaceAcortado; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
}
