-- Crea la base de FrezCo en la instancia de SQL Server compartida con el Sistema de
-- Gestion de Obras. Flyway se encarga del esquema; esto solo crea el contenedor.
-- Idempotente: se puede correr todas las veces que haga falta.

IF DB_ID('$(DB_NAME)') IS NULL
BEGIN
    PRINT 'Creando base $(DB_NAME)';
    EXEC('CREATE DATABASE [$(DB_NAME)]');
END
ELSE
    PRINT 'La base $(DB_NAME) ya existe';
GO
