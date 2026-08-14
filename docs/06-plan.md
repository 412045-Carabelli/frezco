# 06 — Plan de implementación

Siete etapas. Cada una deja algo funcionando y verificable. No pasar a la siguiente sin
terminar la anterior.

Estimación total: **18 a 25 horas efectivas** con asistencia de IA.

---

## Etapa 1 — Esqueleto y modelo (2-3 h)

- [x] Proyecto Spring Boot con las dependencias: Web, Data JPA, Validation, Security,
      Flyway, driver de SQL Server
- [x] Proyecto Angular standalone con PrimeNG v19
- [x] `docker-compose.yml` con backend, frontend y conexión a la base existente
- [x] Migración `V1__esquema_inicial.sql` con las cinco tablas
- [x] Migración `V2__datos_semilla.sql` con proveedor, Refuerzo Stock y Consumo Propio
- [x] Entidades JPA y repositorios
- [x] Autenticación básica con credenciales de variables de entorno

**Verificable:** la app levanta, Flyway corre las migraciones, el login funciona.

Estado: backend y frontend compilan. La verificación contra la base queda pendiente de
levantar SQL Server (el stack de obras) en el VPS o en local con Docker.

---

## Etapa 2 — ABMs (2-3 h)

- [ ] Backend: CRUD de productos y cuentas, con las validaciones de cuentas únicas
      por tipo especial
- [ ] Frontend: pantallas de Artículos y Cuentas
- [ ] Cálculo de margen porcentual mostrado en el formulario de artículo

**Verificable:** se pueden cargar los 30 productos y las 30 cuentas a mano.

---

## Etapa 3 — Pedidos y reparto de stock (5-6 h)

**La etapa más delicada del proyecto.** Es donde está toda la lógica no trivial.

- [ ] `PedidoService.crear()` con: resolución de precio por condición y descuento,
      congelado de precio y costo, numeración por prefijo, reparto stock/proveedor
- [ ] Consumo de stock incremental dentro del mismo pedido
- [ ] Anulación de pedidos
- [ ] `GET /api/pedidos/precio` para la vista previa
- [ ] **Tests unitarios de los siete casos de la tabla de `03-reglas-negocio.md`**
      antes de escribir el frontend

**Verificable:** los tests pasan. Cargar un pedido por API y verificar el reparto.

---

## Etapa 4 — Pantalla de carga de pedidos (4-5 h)

- [ ] Formulario de encabezado con precarga de descuento
- [ ] Grilla de líneas con autocompletado de artículo y precio automático
- [ ] Aviso "se pide al proveedor" cuando no hay stock
- [ ] Layout mobile: tarjetas apiladas, botón flotante, pie fijo
- [ ] Listado de pedidos con filtros y anulación

**Verificable:** cargar un pedido completo desde un celular real, sin usar la PC.
Si eso no es cómodo, la etapa no está terminada.

---

## Etapa 5 — Remitos y stock (3-4 h)

- [ ] Endpoint y pantalla de remito de cliente
- [ ] Endpoint y pantalla de remito de proveedor por período, agrupado por fecha
- [ ] CSS de impresión para ambos
- [ ] Consulta de stock y detalle de movimientos por producto
- [ ] Pantalla de Stock

**Verificable:** imprimir un remito desde el celular y que salga prolijo en el PDF.

---

## Etapa 6 — Cuentas corrientes y resumen (3-4 h)

- [ ] Consulta de cuenta corriente unificando pedidos y movimientos, con saldo corriendo
- [ ] Endpoint de saldos de todas las cuentas
- [ ] ABM de cobros y pagos
- [ ] Pantallas de Cuentas corrientes y Cobros y pagos
- [ ] Endpoint y pantalla de Resumen

**Verificable:** el saldo de un cliente coincide con lo calculado a mano sobre sus
pedidos y cobros.

---

## Etapa 7 — Migración, deploy y entrega (2-3 h)

- [ ] Script de importación de la planilla actual (productos, cuentas, pedidos
      históricos con su reparto ya calculado)
- [ ] Verificación cruzada: los totales del sistema tienen que coincidir con los de la
      planilla. Ventas, margen y saldos por cuenta.
- [ ] Deploy en el VPS con Docker Compose
- [ ] Cron de backup diario de la base, retención 7 días
- [ ] Prueba de restauración del backup (hacerla de verdad, una vez)
- [ ] Guía de uso de una página para la usuaria

**Verificable:** el sistema en producción muestra los mismos números que la planilla.

---

## Criterio de terminado

El proyecto está listo cuando:

1. Los totales del sistema coinciden con los de la planilla sobre los datos migrados.
2. Se puede cargar un pedido completo desde el celular en menos de un minuto.
3. El remito de cliente se puede compartir por WhatsApp sin pasos intermedios raros.
4. El backup corrió al menos una vez y se probó restaurarlo.

---

## Orden recomendado de trabajo con Claude Code

Las etapas 1, 2, 5 y 7 son mecánicas: se pueden delegar de a bloques grandes.

La etapa 3 conviene hacerla al revés: **escribir primero los tests** de los siete casos
de reparto, y recién después pedir la implementación. Es la única parte donde un error
sutil pasa desapercibido y aparece meses después como "el stock no da".

La etapa 4 requiere iteración visual. Conviene hacerla en pasos chicos y probar en un
celular real, no en el emulador del navegador.
