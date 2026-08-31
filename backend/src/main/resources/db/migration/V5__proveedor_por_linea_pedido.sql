-- El proveedor del articulo (V4) resulto ser solo un default: la usuaria necesita poder
-- elegir a que proveedor le pide cada linea al cargar el pedido, porque un mismo articulo
-- se le puede terminar pidiendo a proveedores distintos segun el momento. El proveedor
-- se elige y se congela en la linea, igual que precio y costo: nunca se recalcula.

ALTER TABLE pedido_linea ADD proveedor_id BIGINT NULL;
GO

UPDATE pedido_linea SET proveedor_id = (
    SELECT proveedor_id FROM producto WHERE producto.id = pedido_linea.producto_id
) WHERE unidades_proveedor > 0;
GO

ALTER TABLE pedido_linea ADD CONSTRAINT fk_linea_proveedor FOREIGN KEY (proveedor_id) REFERENCES cuenta(id);
GO
