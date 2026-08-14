-- Cuentas obligatorias del sistema. Debe existir exactamente una de cada tipo especial:
-- no se crean ni se dan de baja desde la UI. Ver docs/02-modelo-datos.md.
--
-- El nombre del proveedor es provisorio: se corrige desde la pantalla de Cuentas.

INSERT INTO cuenta (nombre, tipo, zona, descuento_pct, activo) VALUES
    (N'Proveedor',      'PROVEEDOR', NULL, 0, 1),
    (N'Refuerzo Stock', 'REFUERZO',  NULL, 0, 1),
    (N'Consumo Propio', 'CONSUMO',   NULL, 0, 1);
