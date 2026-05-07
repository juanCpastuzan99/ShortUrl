# Bases de Datos — Datos pre-cargados

Esta carpeta contiene los **scripts de las 3 BD** con datos de ejemplo, según pidió el docente.

| Archivo | BD | Contenido |
|---------|-----|-----------|
| `mysql_dump.sql` | MySQL | Crea base `acortador_enlaces` + tabla `enlaces` con 7 enlaces |
| `mongodb_export.json` | MongoDB | Colección `link_metadata` con 7 documentos (imagen + descripción) |
| `redis_commands.txt` | Redis | 4 entradas pre-cacheadas (URLs originales con ≥ 50 caracteres) |

> Los `short_code` son consistentes entre las 3 fuentes:
> `4c92ab`, `5d83bc`, `6e74cd`, `7f65de`, `8a56ef`, `9b47f0`, `0c38a1`

---

## 🔑 Distribución de datos por BD (según requerimiento)

| BD | Almacena |
|----|----------|
| **MySQL** | `enlaceOriginal`, `enlaceAcortado` (+ id, fecha) |
| **MongoDB** | `short_code`, `image_url`, `description` |
| **Redis** | Caché `link:{shortCode}` → URL original — **solo para URLs ≥ 50 chars** |

---

## 📥 Cómo cargarlos manualmente (opcional)

> ⚠️ **No es necesario hacerlo**: la app las inicializa automáticamente al arrancar si están vacías.
> Solo úsalo si quieres pre-cargarlas antes del primer arranque.

### MySQL
```powershell
mysql -u root -p < mysql_dump.sql
```

### MongoDB
```powershell
mongoimport --db ShortUrle --collection link_metadata --file mongodb_export.json --jsonArray
```

### Redis / Memurai
```powershell
Get-Content redis_commands.txt | redis-cli
```
