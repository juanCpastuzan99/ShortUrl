package com.parcial.urlshortener.adapters.mysql;

import com.parcial.urlshortener.domain.model.Link;
import com.parcial.urlshortener.domain.ports.output.LinkRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MySqlLinkRepositoryAdapter implements LinkRepositoryPort {

    private final LinkJpaRepository jpaRepository;

    public MySqlLinkRepositoryAdapter(LinkJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Link save(Link link) {
        LinkEntity entity = toEntity(link);
        LinkEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Link> findByShortCode(String shortCode) {
        return jpaRepository.findByEnlaceAcortado(shortCode).map(this::toDomain);
    }

    @Override
    public List<Link> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void incrementClickCount(String shortCode) {
        // No se persiste el contador en MySQL en este esquema
    }

    private LinkEntity toEntity(Link link) {
        LinkEntity entity = new LinkEntity();
        entity.setId(link.getId());
        entity.setEnlaceOriginal(link.getOriginalUrl());
        entity.setEnlaceAcortado(link.getShortCode());
        entity.setCreadoEn(link.getCreatedAt());
        return entity;
    }

    private Link toDomain(LinkEntity entity) {
        Link link = new Link();
        link.setId(entity.getId());
        link.setShortCode(entity.getEnlaceAcortado());
        link.setOriginalUrl(entity.getEnlaceOriginal());
        link.setShortUrl(null);
        link.setCreatedAt(entity.getCreadoEn());
        link.setClickCount(0L);
        return link;
    }
}
