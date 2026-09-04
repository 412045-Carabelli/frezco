-- Telefono, direccion y email de contacto para clientes y proveedores. Opcionales: se pueden
-- dejar en blanco y cargar despues.

ALTER TABLE cuenta ADD telefono NVARCHAR(30) NULL;
GO

ALTER TABLE cuenta ADD direccion NVARCHAR(160) NULL;
GO

ALTER TABLE cuenta ADD email NVARCHAR(120) NULL;
GO
