# FrezCo — Sistema de gestión

Sistema web de gestión para un emprendimiento chico de venta de productos congelados.
Reemplaza una planilla de Excel. **Un solo usuario** (la dueña del emprendimiento),
carga desde celular y desde PC.

## Principio rector

**Simplicidad sobre completitud.** Este sistema tiene un alcance deliberadamente
acotado y un presupuesto muy bajo. Ante dos soluciones que cumplen el requisito,
elegir siempre la más simple. En particular:

- No introducir abstracciones "por si acaso" (interfaces con una sola implementación,
  capas de servicio que solo delegan, DTOs que replican la entidad sin transformarla).
- No agregar funcionalidad que no esté en `docs/01-alcance.md`.
- Si algo parece requerir una tabla nueva, revisar primero si sale de un query.

El volumen real es de ~35 pedidos y ~100 líneas por mes, con 30 productos y 30 cuentas.
No optimizar para escala. Los cálculos agregados (stock, saldos, totales) se hacen
con queries sobre los movimientos, no con tablas de saldos denormalizadas.

## Stack

| Capa | Tecnología |
|---|---|
| Backend | Spring Boot 3.3.5, Java 17 |
| Persistencia | Spring Data JPA + SQL Server |
| Migraciones | Flyway |
| Frontend | Angular 19 standalone components, signals |
| UI | PrimeNG v19 + Tailwind |
| Deploy | Docker Compose en VPS existente |

Java 17 y no 21: es la versión que usa el Sistema de Gestión de Obras en el mismo VPS.
Subir de versión es cambiar `java.version` en el pom y el tag del Dockerfile.

La base de datos SQL Server ya existe en el VPS (la usa el Sistema de Gestión de Obras).
Este proyecto usa una **base separada** en la misma instancia y se engancha a la red
Docker `sgo_backend` de ese stack.

## Estructura del repositorio

```
backend/          Spring Boot
  src/main/java/ar/frezco/
    producto/     entidad, repository, service, controller
    cuenta/
    pedido/       incluye la lógica de reparto stock/proveedor
    movimiento/   cobros y pagos
    stock/        consultas de stock (sin entidad propia)
    remito/       armado de remitos cliente y proveedor
    resumen/      dashboard
    cuentacorriente/  estados de cuenta por tipo de cuenta
    config/       seguridad, CORS, excepciones
  src/main/resources/db/migration/   Flyway
frontend/         Angular
  src/app/
    features/     una carpeta por pantalla
    core/         servicios HTTP, interceptores, guard
    shared/       componentes reutilizables
backup/           backup diario de la base
db/               creación de la base e importación de la planilla
docs/             documentación funcional y técnica
```

## Dónde vive la lógica no trivial

El precio de una línea y el reparto stock/proveedor varían según el tipo de cuenta del
pedido. Cada variante es una estrategia inyectada por Spring, no un `if` dentro del
servicio:

- `pedido/precio/` — `PrecioDeVenta` (cliente) y `PrecioAlCosto` (refuerzo y consumo).
- `pedido/reparto/` — `RepartoDeSalida` (cliente y consumo) y `RepartoDeEntrada` (refuerzo).
- `cuentacorriente/` — una estrategia por tipo de cuenta para las reglas de debe/haber.

Agregar un tipo de cuenta es agregar una clase, no editar un `switch`. Los siete casos de
reparto de `docs/03-reglas-negocio.md` están cubiertos por tests: si tocás esa lógica,
tienen que seguir pasando.

## Documentación

Leer en este orden antes de escribir código:

1. `docs/01-alcance.md` — qué se construye y qué queda explícitamente afuera
2. `docs/02-modelo-datos.md` — esquema de tablas y decisiones de modelado
3. `docs/03-reglas-negocio.md` — **el documento más importante**: precios, reparto
   stock/proveedor, cuentas corrientes. Toda la lógica no trivial está acá.
4. `docs/04-api.md` — endpoints REST
5. `docs/05-pantallas.md` — pantallas y comportamiento de UI
6. `docs/07-decisiones-pendientes.md` — puntos sin confirmar con el cliente
7. `docs/08-guia-uso.md` — guía de uso para la usuaria

El plan de trabajo por etapas está en `docs/06-plan.md`.

## Skills a consultar

- `angular-primeng-patterns` para todo el frontend
- `springboot-gof-patterns` al agregar servicios o refactorizar el backend

## Convenciones

**Nomenclatura de dominio en neutro.** Las entidades se llaman `Producto`, `Cuenta`,
`Pedido`, `Movimiento`. Nunca `Congelado`, `FrezCo` ni nombres del rubro. El nombre
comercial, el logo y los colores viven en configuración, no en el código. Esto permite
reutilizar el sistema para otro emprendimiento sin tocar el dominio.

**Idioma.** Código, nombres de tablas y columnas en español (el dominio es en español
y el cliente lee los remitos). Comentarios en español. Sin mezclar.

**Importes.** `DECIMAL(14,2)` en base, `BigDecimal` en Java. Nunca `double` ni `float`.

**Fechas.** `LocalDate` para fechas de negocio (fecha de pedido, fecha de cobro).
`Instant` solo para auditoría (`creado_en`).

**Precios y costos congelados.** Al guardar una línea de pedido se copia el precio y
el costo vigentes. Nunca se recalculan hacia atrás leyendo el producto: si mañana
cambia el precio, los pedidos viejos tienen que seguir mostrando lo que se cobró.

**Anulación, no borrado.** Los pedidos no se eliminan ni se editan. Se marcan
`anulado = true`. Todos los cálculos ignoran los anulados.

**Sin usuarios.** Usuario y contraseña únicos en variables de entorno. Autenticación
básica con sesión. Sin tabla de usuarios, sin roles, sin recuperación de contraseña.

## Comandos

```bash
# Backend
cd backend && ./mvnw spring-boot:run
cd backend && ./mvnw test

# Frontend
cd frontend && npm start
cd frontend && npm run build

# Todo junto
docker compose up --build
```

## Qué NO hacer

- No implementar edición de pedidos ya guardados.
- No crear tabla de stock. El stock es un query.
- No crear módulo de análisis de ventas ni sugerencia de compra (fuera de alcance).
- No integrar AFIP ni facturación electrónica.
- No agregar gestión de usuarios, roles ni permisos.
- No usar librerías de generación de PDF. Los remitos son HTML con `@media print`.
