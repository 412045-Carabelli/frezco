-- Con varios proveedores puede haber articulos parecidos de distinto origen (ej. "Brocoli 1 Kg"
-- de dos proveedores): forzar nombre unico obligaba a inventar sufijos artificiales.

ALTER TABLE producto DROP CONSTRAINT uq_producto_nombre;
GO
