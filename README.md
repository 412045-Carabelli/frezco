# FrezCo

Sistema web de stock y pedidos para un emprendimiento chico de venta de productos
congelados. Reemplaza una planilla de Excel. Un solo usuario, carga desde celular y PC.

## Stack

Spring Boot 3.3.5 (Java 17) · Spring Data JPA · SQL Server · Flyway · Angular 19
standalone con PrimeNG v19 y Tailwind · Docker Compose sobre el VPS existente.

La instancia de SQL Server es la misma que usa el Sistema de Gestión de Obras; este
proyecto usa una base separada.

## Estructura

```
backend/       Spring Boot (monolito, un módulo por dominio)
frontend/      Angular standalone
backup/        backup diario de la base, retención 7 días
db/            creación de la base e importación de la planilla
docs/          documentación funcional y técnica
```

## Documentación

| Documento | Contenido |
|---|---|
| [docs/01-alcance.md](docs/01-alcance.md) | qué se construye y qué queda afuera |
| [docs/02-modelo-datos.md](docs/02-modelo-datos.md) | esquema de tablas y decisiones de modelado |
| [docs/03-reglas-negocio.md](docs/03-reglas-negocio.md) | precios, reparto stock/proveedor, cuentas corrientes |
| [docs/04-api.md](docs/04-api.md) | endpoints REST |
| [docs/05-pantallas.md](docs/05-pantallas.md) | pantallas y comportamiento de UI |
| [docs/06-plan.md](docs/06-plan.md) | plan de implementación por etapas |
| [docs/07-decisiones-pendientes.md](docs/07-decisiones-pendientes.md) | puntos sin confirmar con el cliente |
| [docs/08-guia-uso.md](docs/08-guia-uso.md) | guía de uso para la usuaria |

Las convenciones de código están en [CLAUDE.md](CLAUDE.md).

## Puesta en marcha

```bash
cp .env.example .env   # completar credenciales
docker compose up --build
```

El stack se engancha a la red Docker `sgo_backend` del Sistema de Gestión de Obras, que
es donde vive SQL Server. Esa red tiene que existir antes de levantar.

Desarrollo local:

```bash
cd backend && ./mvnw spring-boot:run
cd frontend && npm start
```

## Carga inicial

La planilla actual se importa una sola vez con el script de
[db/importar](db/importar/README.md), que genera el SQL a partir de CSV.
