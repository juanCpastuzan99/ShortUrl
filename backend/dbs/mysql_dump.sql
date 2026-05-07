-- ============================================================
--  Acortador de Enlaces — MySQL
--  Base de datos: acortador_enlaces
--  Tabla:        enlaces  (solo enlaceOriginal + enlaceAcortado)
--  La imagen y descripción se almacenan en MongoDB (link_metadata).
-- ============================================================

CREATE DATABASE IF NOT EXISTS acortador_enlaces
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE acortador_enlaces;

DROP TABLE IF EXISTS enlaces;

CREATE TABLE enlaces (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    enlaceOriginal  VARCHAR(2048) NOT NULL,
    enlaceAcortado  VARCHAR(50)   NOT NULL UNIQUE,
    creadoEn        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_enlace_acortado (enlaceAcortado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ---- Datos de ejemplo ----
INSERT INTO enlaces (enlaceOriginal, enlaceAcortado, creadoEn) VALUES
('https://www.wikipedia.org/wiki/Arquitectura_hexagonal',     '4c92ab', '2026-04-20 10:05:00'),
('https://spring.io/projects/spring-boot',                    '5d83bc', '2026-04-21 11:30:00'),
('https://www.mongodb.com/docs/manual/introduction/',         '6e74cd', '2026-04-22 14:15:00'),
('https://redis.io/docs/latest/develop/get-started/',         '7f65de', '2026-04-23 09:00:00'),
('https://dev.mysql.com/doc/refman/8.0/en/introduction.html', '8a56ef', '2026-04-24 16:45:00'),
('https://docs.docker.com/get-started/overview/',             '9b47f0', '2026-04-25 08:20:00'),
('https://www.youtube.com/watch?v=dQw4w9WgXcQ',               '0c38a1', '2026-04-26 20:00:00');
