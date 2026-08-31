# 01 — Alcance funcional

## Contexto del negocio

Emprendimiento de reventa de productos congelados (frutas, pulpas, verduras).
Compra a **varios proveedores** (cada artículo tiene el suyo fijo) y vende a clientes
finales de dos zonas geográficas.

Modo de operación particular y central para entender el sistema: **no trabaja con
stock permanente**. La mayoría de las ventas se piden al proveedor a medida que
entran. Solo mantiene stock de algunos productos que compra de más ("refuerzo de
stock") para poder entregar en el momento.

Volumen de referencia (5 semanas de operación real):

- 34 pedidos de venta, 67 líneas
- 28 clientes, 28 productos distintos vendidos
- Ticket promedio ~$19.000
- Margen bruto ~28%

## Módulos incluidos

### Artículos

ABM de productos. Campos: nombre, categoría, proveedor, kg, litros, costo, y tres precios
de venta (minorista, mayorista, por cantidad). Baja lógica con `activo`.

El proveedor es el que le vende ese artículo a la usuaria: define a quién se le pide la
mercadería que no sale de stock (ver reparto stock/proveedor) y de quién es la deuda que
se acumula en la cuenta corriente correspondiente.

Los precios los carga la usuaria a mano. **No hay motor de reglas de precios**: son
tres columnas editables. El sistema puede sugerir el precio calculado a partir del
costo y un margen, pero el valor final lo escribe ella (hoy en la planilla redondea
los precios a mano).

### Cuentas

ABM de cuentas. Una cuenta puede ser de cuatro tipos:

| Tipo | Descripción |
|---|---|
| `CLIENTE` | Cliente final |
| `PROVEEDOR` | Un proveedor (puede haber varios) |
| `REFUERZO` | Cuenta especial "Refuerzo Stock" |
| `CONSUMO` | Cuenta especial "Consumo Propio" |

Campos: nombre, tipo, zona, descuento por defecto (%), activo.

Las cuentas `REFUERZO` y `CONSUMO` no son clientes reales: son etiquetas que permiten
cargar movimientos de mercadería usando la misma pantalla de pedidos. Ver
`03-reglas-negocio.md`.

### Pedidos

Pantalla principal del sistema. Carga en grilla:

1. Se elige la cuenta (con búsqueda por nombre).
2. Se elige la condición de venta: minorista (por defecto), mayorista, o por cantidad.
3. Se cargan líneas: buscador de artículo, cantidad. El precio se completa solo según
   la condición.
4. Hay un campo de descuento en porcentaje, que viene precargado con el descuento de
   la cuenta y se puede modificar.
5. Al guardar, el sistema calcula automáticamente cuánto de cada línea sale de stock
   y cuánto hay que pedirle al proveedor.

Los pedidos no se editan. Si hay un error, se anulan y se cargan de nuevo.

### Remitos

Cada pedido genera dos remitos imprimibles:

- **Remito de cliente**: las líneas del pedido con precios de venta y total.
- **Remito de proveedor**: solo las líneas (o partes de líneas) que hay que pedirle,
  con costos, filtrado por un proveedor a la vez (lo define el artículo). Se puede ver
  por pedido individual o consolidado por período.

Ambos son pantallas HTML con estilos de impresión. La usuaria imprime o guarda como
PDF desde el navegador, o comparte desde el celular.

### Stock

Listado de productos con: entradas acumuladas, salidas acumuladas y stock actual.
Detalle de movimientos por producto.

El stock no se guarda en una tabla. Se calcula sumando las entradas y restando las
salidas de las líneas de pedidos no anulados.

### Cuentas corrientes

Estado de cuenta por cuenta seleccionada, con filtro de período:

- Detalle cronológico de movimientos (debe / haber / saldo acumulado)
- Saldo final

Aplica a clientes (deben por ventas, pagan con cobros), a cada proveedor (se le debe
por la mercadería de sus artículos, se le paga con pagos) y a la cuenta Refuerzo Stock
(muestra acumulado todo lo pedido de más, sin distinguir proveedor).

### Cobros y pagos

Registro simple de movimientos de caja: fecha, tipo (cobro / pago), cuenta, importe,
observación. Impactan en el saldo de la cuenta corriente correspondiente.

### Resumen

Pantalla inicial con los indicadores del mes en curso:

- Ventas del período
- Margen del período (ventas − costos)
- Saldo total a cobrar de clientes
- Saldo a pagar a proveedores (suma de todos)
- Productos con stock bajo (opcional, umbral fijo)

## Fuera de alcance

Estas funcionalidades **no se implementan** en esta entrega. Están documentadas para
que no se cuelen por accidente:

| Funcionalidad | Motivo |
|---|---|
| Análisis de ventas (rankings por producto / cliente / zona) | Diferido a segunda etapa |
| Sugerencia de pedido de refuerzo por proyección | No hay historial suficiente para proyectar |
| Módulo de gráficas / multimedia / flyers | Requisito sin definir |
| Edición de pedidos guardados | Se anula y se recarga |
| Facturación electrónica AFIP | Fuera de presupuesto |
| Múltiples usuarios, roles, permisos | Un solo usuario |
| Listas de precios con vigencia histórica | Tres columnas editables alcanzan |

## Restricciones no funcionales

- **Mobile first en la carga de pedidos.** Es donde el sistema tiene que ganarle a la
  planilla. La grilla tiene que ser usable con una mano en un celular.
- El resto de las pantallas puede asumir pantalla de escritorio.
- Backup automático diario de la base con retención de 7 días, configurado en el
  deploy.
- Sin requisitos de performance: el volumen es despreciable.
