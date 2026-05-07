package com.parcial.urlshortener.infrastructure.adapters.output.mysql;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LinkJpaRepository extends JpaRepository<LinkEntity, Long> {
    Optional<LinkEntity> findByEnlaceAcortado(String enlaceAcortado);
}
