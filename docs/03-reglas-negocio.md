# 03 — Reglas de negocio

Este es el documento más importante del proyecto. Toda la lógica que no es un ABM
trivial está acá.

---

## 1. Cálculo del precio de una línea

Al agregar una línea al pedido:

```
precio_base = producto.precio_minorista   si pedido.condicion = MINORISTA
              producto.precio_mayorista   si pedido.condicion = MAYORISTA
              producto.precio_cantidad    si pedido.condicion = CANTIDAD

precio_unitario = redondear(precio_base * (1 - pedido.descuento_pct / 100), 2)
costo_unitario  = producto.costo
```

El descuento se aplica a nivel pedido, no por línea. Si cambia el descuento del
pedido, se recalculan todas las líneas antes de guardar.

`descuento_pct` se precarga con `cuenta.descuento_pct` al elegir la cuenta, y la
usuaria lo puede sobrescribir. Una vez guardado el pedido, el valor queda fijo.

**Importante:** para pedidos de tipo `REFUERZO` y `CONSUMO` no hay precio de venta.
En esos casos `precio_unitario = costo_unitario` y el descuento se ignora (se guarda
en 0). Esos pedidos no cuentan como venta en ningún cálculo.

---

## 2. Reparto entre stock y proveedor

**El corazón del sistema.** Se ejecuta al guardar el pedido, línea por línea, en orden.

### Regla general

El negocio no mantiene stock permanente: la mayoría de lo que vende se lo pide al
proveedor a medida que entran los pedidos. Pero a veces compra de más para tener
disponibilidad. Entonces:

> Si hay stock del producto, la venta sale de ahí y **no** se le pide al proveedor.
> Si no hay, o no alcanza, la diferencia se le pide al proveedor.

### Algoritmo

Para cada línea, según el tipo de cuenta del pedido:

**`CLIENTE` y `CONSUMO`** (movimientos de salida):

```
disponible          = stock_actual(producto)      // ver query en 02-modelo-datos.md
salida_stock        = min(unidades, max(disponible, 0))
entrada_stock       = 0
unidades_proveedor  = unidades - salida_stock
```

**`REFUERZO`** (movimiento de entrada):

```
entrada_stock       = unidades
salida_stock        = 0
unidades_proveedor  = unidades
```

Es decir, un refuerzo se le pide entero al proveedor y entra entero a stock.

### Detalle crítico: el stock se consume dentro del mismo pedido

Si un pedido tiene dos líneas del mismo producto, o si se cargan varias líneas en una
misma transacción, el stock disponible tiene que ir bajando a medida que se procesan
las líneas. Calcular todas las líneas contra el stock inicial produciría un reparto
incorrecto.

Implementación: leer el stock una vez al inicio del guardado, mantener un mapa
`producto_id -> disponible` en memoria, y descontarlo a medida que se procesa cada
línea.

### Casos de referencia

Tomados de la operación real, sirven como tests:

| Caso | Stock previo | Unidades | salida_stock | unidades_proveedor |
|---|---|---|---|---|
| Venta sin stock | 0 | 1 | 0 | 1 |
| Venta con stock suficiente | 3 | 1 | 1 | 0 |
| Venta con stock parcial | 1 | 4 | 1 | 3 |
| Venta con stock exacto | 2 | 2 | 2 | 0 |
| Refuerzo | 0 | 3 | 0 | 3 (y entrada_stock = 3) |
| Consumo propio con stock | 4 | 1 | 1 | 0 |
| Consumo propio sin stock | 0 | 1 | 0 | 1 |

---

## 3. Cuentas corrientes

Cada cuenta tiene un estado de cuenta con movimientos en orden cronológico, columnas
debe / haber y saldo acumulado.

### Cuenta de tipo CLIENTE

| Origen | Debe | Haber |
|---|---|---|
| Pedido de venta | `SUM(unidades * precio_unitario)` | — |
| Movimiento `COBRO` | — | `importe` |

`saldo = SUM(debe) - SUM(haber)`. Saldo positivo = el cliente debe plata.

### Cuenta de tipo PROVEEDOR

Acá se acumula todo lo que se le pidió, venga de donde venga: ventas que no salieron
de stock, refuerzos y consumos propios.

| Origen | Debe | Haber |
|---|---|---|
| Cualquier pedido con `unidades_proveedor > 0` | `SUM(unidades_proveedor * costo_unitario)` | — |
| Movimiento `PAGO` | — | `importe` |

Saldo positivo = se le debe al proveedor.

**Nota:** el pedido puede ser de cualquier cuenta (un cliente, refuerzo o consumo).
Lo que define si impacta en el proveedor es `unidades_proveedor > 0`, no la cuenta
del pedido.

### Cuenta de tipo REFUERZO

Es una vista informativa: muestra acumulado todo lo que se pidió de más al proveedor
para tener stock disponible.

| Origen | Debe | Haber |
|---|---|---|
| Pedido de la cuenta Refuerzo | `SUM(unidades_proveedor * costo_unitario)` | — |

No tiene cobros ni pagos. Su "saldo" es el total histórico de refuerzos, no una deuda.
Ese importe **ya está contemplado dentro del saldo del proveedor**: no se suma dos
veces, es otra forma de mirar lo mismo. Dejarlo aclarado en la pantalla.

### Cuenta de tipo CONSUMO

Igual que refuerzo: vista informativa del costo de la mercadería consumida
internamente. No genera deuda con nadie.

| Origen | Debe | Haber |
|---|---|---|
| Pedido de la cuenta Consumo | `SUM(unidades * costo_unitario)` | — |

---

## 4. Remitos

### Remito de cliente

Se emite por pedido. Incluye todas las líneas del pedido con `unidades`,
`precio_unitario` e importe de línea. Total al pie.

Se emite para pedidos de cuentas tipo `CLIENTE`. Los pedidos de refuerzo y consumo no
generan remito de cliente.

### Remito de proveedor

Se emite por período (no por pedido), agrupado por fecha. Incluye **solo las líneas
con `unidades_proveedor > 0`**, de cualquier pedido: ventas, refuerzos y consumos.

Por cada línea: producto, `unidades_proveedor`, `costo_unitario`, importe.
Subtotal por fecha y total general del período.

Si dos pedidos del mismo día piden el mismo producto, las líneas se consolidan en una
sola con la suma de unidades.

---

## 5. Stock

```
stock_actual(producto) = SUM(entrada_stock) - SUM(salida_stock)
                         sobre pedido_linea de pedidos con anulado = 0
```

El detalle de movimientos de un producto lista las líneas que lo afectan, ordenadas
por fecha, con el saldo de stock corriendo.

No se permite stock negativo por construcción: `salida_stock` nunca supera el
disponible, el excedente va a `unidades_proveedor`.

---

## 6. Resumen (dashboard)

Todos los indicadores sobre pedidos con `anulado = 0`, filtrados por el período
seleccionado (por defecto el mes en curso).

| Indicador | Cálculo |
|---|---|
| Ventas | `SUM(unidades * precio_unitario)` de pedidos de cuentas `CLIENTE` |
| Costo | `SUM(unidades * costo_unitario)` de pedidos de cuentas `CLIENTE` |
| Margen | Ventas − Costo |
| Margen % | Margen / Ventas |
| Saldo a cobrar | Suma de saldos positivos de todas las cuentas `CLIENTE` |
| Saldo a pagar | Saldo de la cuenta `PROVEEDOR` |

Los pedidos de refuerzo y consumo **no** entran en ventas, costo ni margen.

---

## 7. Anulación de pedidos

Marcar `anulado = true`. No borrar filas ni tocar las líneas.

Efectos automáticos, sin código adicional, porque todo se calcula con queries que
filtran por `anulado = 0`:

- El stock vuelve al valor previo.
- Los saldos de cuenta corriente se ajustan.
- El pedido desaparece de los remitos.
- Los indicadores del resumen se recalculan.

No se anula un pedido de refuerzo si eso dejaría stock negativo en la práctica; el
sistema no lo impide, pero conviene mostrar una advertencia al anular un refuerzo cuya
mercadería ya se vendió.
