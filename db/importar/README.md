# Importación de la planilla

Se corre **una sola vez**, sobre la base recién migrada por Flyway y vacía de datos.
No existe pantalla de importación en el sistema: es un script y se descarta.

## 1. Exportar la planilla a CSV

Una hoja por archivo, con estas columnas exactas (primera fila = cabecera):

| Archivo | Columnas |
|---|---|
| `productos.csv` | `nombre,categoria,kg,lt,costo,minorista,mayorista,cantidad` |
| `cuentas.csv` | `nombre,tipo,zona,descuento` |
| `pedidos.csv` | `numero,fecha,cuenta,condicion,descuento,observacion` |
| `lineas.csv` | `numero,producto,unidades,precio,costo,entrada,salida,proveedor` |
| `movimientos.csv` | `fecha,tipo,cuenta,importe,observacion` |

Detalles:

- Fechas en formato `aaaa-mm-dd`.
- `tipo` de cuenta: `CLIENTE`, `PROVEEDOR`, `REFUERZO` o `CONSUMO`. Las tres especiales
  ya existen (las creó la migración `V2`): el script solo les corrige el nombre.
- `condicion`: `MINORISTA`, `MAYORISTA` o `CANTIDAD`.
- `numero` de pedido con el formato del sistema: `Vta 001`, `Ref 001`, `Cons 001`.
- En `lineas.csv`, `entrada`, `salida` y `proveedor` son el reparto **ya calculado en la
  planilla**. No se recalculan: el reparto dependía del stock que había ese día.
- Valores con comas, entre comillas dobles.

## 2. Generar el SQL

```bash
node importar.mjs ./datos > carga.sql
```

## 3. Cargar

```bash
sqlcmd -S localhost,1433 -U sa -P "$DB_PASSWORD" -d frezco -i carga.sql
```

Todo va dentro de una transacción: si algo falla, no queda nada a medias.

## 4. Verificación cruzada

El script termina con el total de ventas. Tiene que coincidir con el de la planilla.
Comparar también, desde el sistema:

- **Stock** por artículo, contra la hoja de control.
- **Cuentas corrientes**, saldo por cliente.
- **Resumen** del período completo: ventas y margen.

Si algún total no coincide, corregir el CSV y volver a empezar sobre una base limpia:

```sql
DELETE FROM pedido_linea;
DELETE FROM pedido;
DELETE FROM movimiento;
DELETE FROM cuenta WHERE tipo = 'CLIENTE';
DELETE FROM producto;
```
