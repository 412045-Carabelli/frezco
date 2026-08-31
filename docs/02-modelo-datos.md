# 02 — Modelo de datos

Cinco tablas. Todo lo demás (stock, saldos, totales, remitos, resumen) sale de queries
sobre estas.

## Diagrama conceptual

```
cuenta ────< pedido ────< pedido_linea >──── producto
   │
   └───────< movimiento
```

## DDL

Dialecto SQL Server. Las migraciones van en `backend/src/main/resources/db/migration/`.

```sql
-- V1__esquema_inicial.sql

CREATE TABLE producto (
    id                  BIGINT IDENTITY(1,1) PRIMARY KEY,
    nombre              NVARCHAR(120)  NOT NULL,
    categoria           NVARCHAR(60)   NULL,
    kg                  DECIMAL(10,3)  NULL,
    lt                  DECIMAL(10,3)  NULL,
    costo               DECIMAL(14,2)  NOT NULL DEFAULT 0,
    precio_minorista    DECIMAL(14,2)  NOT NULL DEFAULT 0,
    precio_mayorista    DECIMAL(14,2)  NOT NULL DEFAULT 0,
    precio_cantidad     DECIMAL(14,2)  NOT NULL DEFAULT 0,
    activo              BIT            NOT NULL DEFAULT 1,
    CONSTRAINT uq_producto_nombre UNIQUE (nombre)
);

CREATE TABLE cuenta (
    id                  BIGINT IDENTITY(1,1) PRIMARY KEY,
    nombre              NVARCHAR(120)  NOT NULL,
    tipo                VARCHAR(20)    NOT NULL,  -- CLIENTE | PROVEEDOR | REFUERZO | CONSUMO
    zona                NVARCHAR(60)   NULL,
    descuento_pct       DECIMAL(5,2)   NOT NULL DEFAULT 0,
    activo              BIT            NOT NULL DEFAULT 1,
    CONSTRAINT uq_cuenta_nombre UNIQUE (nombre),
    CONSTRAINT ck_cuenta_tipo CHECK (tipo IN ('CLIENTE','PROVEEDOR','REFUERZO','CONSUMO'))
);

CREATE TABLE pedido (
    id                  BIGINT IDENTITY(1,1) PRIMARY KEY,
    numero              VARCHAR(20)    NOT NULL,  -- 'Vta 001', 'Ref 001', 'Cons 001'
    fecha               DATE           NOT NULL,
    cuenta_id           BIGINT         NOT NULL,
    condicion           VARCHAR(20)    NOT NULL,  -- MINORISTA | MAYORISTA | CANTIDAD
    descuento_pct       DECIMAL(5,2)   NOT NULL DEFAULT 0,
    observacion         NVARCHAR(300)  NULL,
    anulado             BIT            NOT NULL DEFAULT 0,
    creado_en           DATETIME2      NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT fk_pedido_cuenta FOREIGN KEY (cuenta_id) REFERENCES cuenta(id),
    CONSTRAINT uq_pedido_numero UNIQUE (numero),
    CONSTRAINT ck_pedido_condicion CHECK (condicion IN ('MINORISTA','MAYORISTA','CANTIDAD'))
);

CREATE INDEX ix_pedido_fecha  ON pedido(fecha);
CREATE INDEX ix_pedido_cuenta ON pedido(cuenta_id);

CREATE TABLE pedido_linea (
    id                  BIGINT IDENTITY(1,1) PRIMARY KEY,
    pedido_id           BIGINT         NOT NULL,
    producto_id         BIGINT         NOT NULL,
    unidades            DECIMAL(12,2)  NOT NULL,
    precio_unitario     DECIMAL(14,2)  NOT NULL,  -- congelado, ya con descuento aplicado
    costo_unitario      DECIMAL(14,2)  NOT NULL,  -- congelado
    entrada_stock       DECIMAL(12,2)  NOT NULL DEFAULT 0,
    salida_stock        DECIMAL(12,2)  NOT NULL DEFAULT 0,
    unidades_proveedor  DECIMAL(12,2)  NOT NULL DEFAULT 0,
    CONSTRAINT fk_linea_pedido   FOREIGN KEY (pedido_id)   REFERENCES pedido(id),
    CONSTRAINT fk_linea_producto FOREIGN KEY (producto_id) REFERENCES producto(id)
);

CREATE INDEX ix_linea_pedido   ON pedido_linea(pedido_id);
CREATE INDEX ix_linea_producto ON pedido_linea(producto_id);

CREATE TABLE movimiento (
    id                  BIGINT IDENTITY(1,1) PRIMARY KEY,
    fecha               DATE           NOT NULL,
    tipo                VARCHAR(10)    NOT NULL,  -- COBRO | PAGO
    cuenta_id           BIGINT         NOT NULL,
    importe             DECIMAL(14,2)  NOT NULL,
    observacion         NVARCHAR(300)  NULL,
    creado_en           DATETIME2      NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT fk_movimiento_cuenta FOREIGN KEY (cuenta_id) REFERENCES cuenta(id),
    CONSTRAINT ck_movimiento_tipo CHECK (tipo IN ('COBRO','PAGO'))
);

CREATE INDEX ix_movimiento_fecha  ON movimiento(fecha);
CREATE INDEX ix_movimiento_cuenta ON movimiento(cuenta_id);
```

## Decisiones de modelado

### Por qué no hay tabla de stock

El stock actual de un producto es:

```sql
SELECT SUM(l.entrada_stock) - SUM(l.salida_stock)
FROM pedido_linea l
JOIN pedido p ON p.id = l.pedido_id
WHERE l.producto_id = :id AND p.anulado = 0;
```

Con este volumen es instantáneo, y elimina de raíz la posibilidad de que el stock
guardado se desincronice de los movimientos. La planilla actual necesita una hoja de
control justamente porque ese problema existe en Excel.

### Por qué las tres cantidades de la línea se persisten

`entrada_stock`, `salida_stock` y `unidades_proveedor` **se calculan una sola vez, al
guardar el pedido, y no se recalculan nunca**.

El motivo: el reparto entre "sale de stock" y "se pide al proveedor" depende del stock
disponible *en ese momento*. Si el cálculo se hiciera al vuelo, cargar un pedido con
fecha retroactiva o anular un pedido viejo cambiaría el reparto de todos los pedidos
posteriores, y los remitos de proveedor ya enviados dejarían de coincidir.

Consecuencia asumida: si se anula un pedido, el stock se libera pero los pedidos
posteriores mantienen el reparto que tuvieron. Es el comportamiento correcto.

### Por qué el precio y el costo se congelan

`precio_unitario` ya tiene el descuento aplicado. `costo_unitario` es el costo del
producto al momento de la carga. Los precios de la tabla `producto` cambian seguido
(inflación); los pedidos históricos tienen que seguir mostrando lo que efectivamente
se cobró y lo que efectivamente costó.

El margen de un pedido es siempre
`SUM(unidades * (precio_unitario - costo_unitario))`, nunca se lee de `producto`.

### Por qué REFUERZO y CONSUMO son tipos de cuenta

El documento del cliente los describe como "clientes" especiales. Modelarlos como
cuentas permite reutilizar la misma pantalla de carga, la misma tabla de pedidos y la
misma vista de cuenta corriente, en lugar de tener tres flujos separados.

Restricción de negocio: debe existir **exactamente una** cuenta de tipo `REFUERZO` y
una de tipo `CONSUMO`. Se crean en la migración de datos semilla y no se pueden dar de
baja desde la UI. `PROVEEDOR` no tiene esa restricción: el negocio le compra a varios
(ver "Proveedor por artículo" más abajo).

### Numeración de comprobantes

Tres secuencias independientes según el tipo de cuenta del pedido:

| Tipo de cuenta | Prefijo | Ejemplo |
|---|---|---|
| `CLIENTE` | `Vta` | `Vta 001` |
| `REFUERZO` | `Ref` | `Ref 001` |
| `CONSUMO` | `Cons` | `Cons 001` |

Formato: prefijo, espacio, número con 3 dígitos y ceros a la izquierda. Se calcula al
guardar con `MAX(numero)` filtrado por prefijo. Al ser un solo usuario no hay
concurrencia real, pero igual conviene resolverlo dentro de la transacción de guardado.

### Proveedor por línea de pedido

`pedido_linea.proveedor_id` (agregada en `V5__proveedor_por_linea_pedido.sql`) define a
qué proveedor se le pidió esa línea. Se elige al cargar el pedido y se congela ahí,
igual que `precio_unitario` y `costo_unitario`: nunca se recalcula ni se vuelve a leer
del artículo después. Es `NULL` cuando la línea sale entera de stock (no se le pidió
nada a nadie).

`producto.proveedor_id` (agregada antes, en `V4__proveedor_por_articulo.sql`) sigue
existiendo pero cambió de rol: ya **no** es lo que usan la cuenta corriente ni el
remito de proveedor (que ahora filtran por `pedido_linea.proveedor_id`). Es solo el
valor sugerido con el que el frontend precarga la línea al elegir el artículo en Nuevo
Pedido — la usuaria lo puede cambiar ahí mismo, porque un mismo artículo se le puede
terminar pidiendo a proveedores distintos según el momento.

### Nombre de artículo no único

`V6__nombre_producto_no_unico.sql` saca el `UNIQUE (nombre)` de `producto` (estaba en
`V1`). Con varios proveedores puede haber artículos parecidos de origen distinto (ej.
"Brócoli 1 Kg" de dos proveedores) sin recurrir a sufijos artificiales para el nombre.

## Datos semilla

La migración `V2__datos_semilla.sql` debe crear:

- La cuenta del proveedor (`tipo = 'PROVEEDOR'`) — la primera; después se pueden agregar más
  desde la pantalla de Cuentas.
- La cuenta `Refuerzo Stock` (`tipo = 'REFUERZO'`)
- La cuenta `Consumo Propio` (`tipo = 'CONSUMO'`)

## Migración de datos existentes

Hay una planilla con ~90 líneas de movimientos reales, 28 productos y 28 cuentas.
La importación se hace con un script único de carga (CSV → SQL), no con una pantalla
de importación en el sistema. Se corre una sola vez en el arranque.

Los pedidos históricos se importan con sus valores de `entrada_stock`,
`salida_stock` y `unidades_proveedor` ya calculados desde la planilla, no recalculados.
