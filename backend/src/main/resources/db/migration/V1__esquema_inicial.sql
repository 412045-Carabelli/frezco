-- Esquema inicial. Cinco tablas: todo lo demas (stock, saldos, totales, remitos,
-- resumen) sale de queries sobre estas. Ver docs/02-modelo-datos.md.

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
