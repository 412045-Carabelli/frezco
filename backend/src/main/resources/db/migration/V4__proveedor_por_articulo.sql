-- El negocio paso a comprarle a varios proveedores. Antes habia una unica cuenta PROVEEDOR
-- y todo lo pedido se le atribuia a ella; ahora cada articulo define a que proveedor
-- corresponde. Ver docs/03-reglas-negocio.md.

ALTER TABLE producto ADD proveedor_id BIGINT NULL;
GO

UPDATE producto SET proveedor_id = (SELECT TOP 1 id FROM cuenta WHERE tipo = 'PROVEEDOR');
GO

ALTER TABLE producto ALTER COLUMN proveedor_id BIGINT NOT NULL;
GO

ALTER TABLE producto ADD CONSTRAINT fk_producto_proveedor FOREIGN KEY (proveedor_id) REFERENCES cuenta(id);
GO
