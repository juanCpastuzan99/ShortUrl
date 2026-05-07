-- Auto-cargado por Spring Boot al arrancar (solo si la tabla está vacía)
INSERT INTO enlaces (enlaceOriginal, enlaceAcortado, descripcion, creadoEn)
SELECT * FROM (
    SELECT 'https://www.wikipedia.org/wiki/Arquitectura_hexagonal' AS o, '4c92ab' AS s, NULL AS d, '2026-04-20 10:05:00' AS c UNION ALL
    SELECT 'https://spring.io/projects/spring-boot',                  '5d83bc', NULL, '2026-04-21 11:30:00' UNION ALL
    SELECT 'https://www.mongodb.com/docs/manual/introduction/',       '6e74cd', NULL, '2026-04-22 14:15:00' UNION ALL
    SELECT 'https://redis.io/docs/latest/develop/get-started/',       '7f65de', NULL, '2026-04-23 09:00:00' UNION ALL
    SELECT 'https://dev.mysql.com/doc/refman/8.0/en/introduction.html','8a56ef', NULL, '2026-04-24 16:45:00' UNION ALL
    SELECT 'https://docs.docker.com/get-started/overview/',           '9b47f0', NULL, '2026-04-25 08:20:00' UNION ALL
    SELECT 'https://www.youtube.com/watch?v=dQw4w9WgXcQ',             '0c38a1', NULL, '2026-04-26 20:00:00'
) AS seed
WHERE NOT EXISTS (SELECT 1 FROM enlaces);
