# 05 — Pantallas

Angular 17+ standalone components con signals. PrimeNG v17. Consultar la skill
`angular-primeng-patterns` antes de escribir componentes.

## Navegación

Menú lateral colapsable (`p-sidebar` en mobile, fijo en desktop):

```
Resumen
Pedidos          <- pantalla principal
Cobros y pagos
Stock
Cuentas corrientes
Remito proveedor
Artículos
Cuentas
```

---

## 1. Nuevo pedido

**La pantalla más importante del sistema.** Tiene que ser cómoda de usar con una mano
en un celular, parada frente al freezer. Es lo que la planilla no puede hacer, y es lo
que decide si la usuaria adopta el sistema o vuelve a Excel.

### Estructura

**Encabezado**

- Fecha (`p-calendar`, por defecto hoy)
- Cuenta (`p-autoComplete` con búsqueda por nombre, mínimo 1 carácter)
- Condición (`p-selectButton`: Minorista / Mayorista / Por cantidad, default Minorista)
- Descuento % (`p-inputNumber`, precargado con el descuento de la cuenta)

Al elegir la cuenta, precargar el descuento. Si la cuenta es de tipo `REFUERZO` o
`CONSUMO`, ocultar condición y descuento (no aplican).

**Grilla de líneas**

Una fila por línea, con:

- Artículo (`p-autoComplete`, búsqueda por nombre)
- Unidades (`p-inputNumber` con botones + / −)
- Precio unitario (solo lectura, se completa al elegir el artículo)
- Importe (solo lectura)
- Botón eliminar

Al elegir el artículo, llamar a `GET /api/pedidos/precio` y mostrar el precio.
Si la respuesta indica `stockDisponible < unidades`, mostrar un chip discreto
"se pide al proveedor". Es informativo, no bloquea.

**Pie fijo**

Barra fija abajo con el total acumulado y el botón Guardar. En mobile no puede quedar
tapada por el teclado.

### Comportamiento en mobile

- Botón flotante "+ Artículo" que agrega una fila y abre el buscador enfocado.
- Filas apiladas en tarjetas, no tabla horizontal con scroll.
- Al agregar una línea, hacer scroll automático a la nueva fila.
- Teclado numérico (`inputmode="decimal"`) en el campo de unidades.

### Al guardar

`POST /api/pedidos`. Si sale bien, navegar directo al remito de cliente del pedido
recién creado. Ese es el flujo natural: cargo la venta, mando el remito.

---

## 2. Listado de pedidos

Tabla con filtro de período y de cuenta. Columnas: número, fecha, cuenta, condición,
total, estado.

Acciones por fila: ver detalle, ver remito, anular (con confirmación).

Los pedidos anulados se muestran tachados y solo si el filtro "incluir anulados" está
activo.

---

## 3. Remito de cliente

Vista de impresión. Encabezado con nombre del negocio, número de comprobante, fecha y
cliente. Tabla de líneas con unidades, precio unitario e importe. Total al pie.

Botón "Imprimir" que dispara `window.print()`.

CSS con `@media print`: ocultar menú, botones y todo lo que no sea el remito. Ancho
fijo, márgenes de página. **No usar librerías de PDF.**

En mobile, el usuario puede compartir usando el diálogo nativo de impresión →
"Guardar como PDF" → compartir por WhatsApp.

---

## 4. Remito de proveedor

Igual concepto, pero con filtro de período. Agrupado por fecha con subtotal por día y
total del período. Muestra costos, no precios de venta.

---

## 5. Stock

Tabla: producto, categoría, entradas, salidas, stock actual, última entrada, última
venta.

Ordenable por stock. Resaltar en color las filas con stock 0.

Click en una fila abre el detalle de movimientos de ese producto (fecha, comprobante,
cuenta, entrada, salida, saldo).

---

## 6. Cuentas corrientes

Selector de cuenta arriba (`p-dropdown` agrupado por tipo) y filtro de período.

Tabla: fecha, origen, detalle, debe, haber, saldo. Fila de totales al pie.

Para las cuentas `REFUERZO` y `CONSUMO`, mostrar un aviso arriba aclarando que es una
vista informativa y que esos importes ya están incluidos en el saldo del proveedor.

Arriba de todo, una vista rápida de saldos: lista de clientes con saldo pendiente,
ordenada de mayor a menor, y el saldo del proveedor.

---

## 7. Cobros y pagos

Formulario simple arriba (fecha, tipo, cuenta, importe, observación) y listado abajo
con filtro de período.

Al elegir la cuenta, mostrar el saldo actual al lado del campo de importe, para que la
usuaria sepa cuánto le deben antes de cargar el cobro.

Borrar con confirmación.

---

## 8. Artículos y 9. Cuentas

ABMs estándar. Tabla con búsqueda, botón nuevo, edición en `p-dialog`, baja lógica con
confirmación.

En Artículos, al editar el costo mostrar debajo de cada precio el margen resultante en
porcentaje. Ayuda a decidir el precio sin calculadora.

---

## 10. Resumen

Pantalla inicial. Selector de período (por defecto mes en curso).

Cuatro tarjetas grandes: Ventas, Margen, A cobrar, A pagar.
Debajo, dos datos secundarios: cantidad de pedidos y ticket promedio.
Al final, lista corta de productos sin stock.

Sin gráficos. No aportan con este volumen y suman peso.

---

## Estilo visual

- El nombre comercial, el logo y el color principal salen de un archivo de
  configuración del frontend, no hardcodeados en los componentes.
- Tema de PrimeNG estándar, sin diseño custom. El presupuesto no lo contempla.
- Densidad compacta en tablas de escritorio, espaciada en mobile.
