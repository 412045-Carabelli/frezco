-- Descuento por defecto de cada articulo. Si es mayor a 0, pisa al descuento
-- general del pedido para esa linea (no son acumulativos). Ver docs/03-reglas-negocio.md.
ALTER TABLE producto ADD descuento_pct DECIMAL(5,2) NOT NULL DEFAULT 0;
