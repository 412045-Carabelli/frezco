# 04 — API REST

Base: `/api`. Todas las respuestas en JSON. Todos los importes como número decimal.

Autenticación: HTTP Basic con usuario y contraseña de variables de entorno, sesión por
cookie. Todos los endpoints requieren autenticación excepto `POST /api/auth/login`.

## Convenciones

- Errores de validación: `400` con `{ "mensaje": "...", "campo": "..." }`
- No encontrado: `404` con `{ "mensaje": "..." }`
- Conflicto de negocio: `409` con `{ "mensaje": "..." }`
- Manejo centralizado con `@RestControllerAdvice`.

---

## Autenticación

```
POST   /api/auth/login          { usuario, clave }        -> 200 | 401
POST   /api/auth/logout                                   -> 204
GET    /api/auth/sesion                                   -> 200 { autenticado }
```

---

## Productos

```
GET    /api/productos                    ?busqueda=&soloActivos=true
GET    /api/productos/{id}
POST   /api/productos
PUT    /api/productos/{id}
DELETE /api/productos/{id}               baja lógica (activo = false)
```

`ProductoDTO`:

```json
{
  "id": 1,
  "nombre": "Arándano 1kg",
  "categoria": "Frutas 1kg",
  "kg": 1.0,
  "lt": null,
  "costo": 11700.00,
  "precioMinorista": 16300.00,
  "precioMayorista": 14500.00,
  "precioCantidad": 15200.00,
  "activo": true
}
```

---

## Cuentas

```
GET    /api/cuentas                      ?tipo=&busqueda=&soloActivas=true
GET    /api/cuentas/{id}
POST   /api/cuentas
PUT    /api/cuentas/{id}
DELETE /api/cuentas/{id}                 baja lógica
```

`CuentaDTO`:

```json
{
  "id": 12,
  "nombre": "Regina Quintanilla",
  "tipo": "CLIENTE",
  "zona": "Córdoba",
  "descuentoPct": 0.00,
  "activo": true
}
```

Reglas: no se permite crear una segunda cuenta de tipo `PROVEEDOR`, `REFUERZO` ni
`CONSUMO`. No se permite dar de baja esas tres cuentas. Devolver `409`.

---

## Pedidos

```
GET    /api/pedidos                      ?desde=&hasta=&cuentaId=&incluirAnulados=false
GET    /api/pedidos/{id}
POST   /api/pedidos
POST   /api/pedidos/{id}/anular          -> 204
```

### Crear pedido

Request:

```json
{
  "fecha": "2026-08-14",
  "cuentaId": 12,
  "condicion": "MINORISTA",
  "descuentoPct": 0,
  "observacion": null,
  "lineas": [
    { "productoId": 3, "unidades": 2 },
    { "productoId": 7, "unidades": 1 }
  ]
}
```

El cliente **no manda precios**. El backend los resuelve según la condición y el
descuento, y ejecuta el reparto stock/proveedor. Ver `03-reglas-negocio.md`.

Response `201`:

```json
{
  "id": 41,
  "numero": "Vta 035",
  "fecha": "2026-08-14",
  "cuenta": { "id": 12, "nombre": "Regina Quintanilla", "tipo": "CLIENTE" },
  "condicion": "MINORISTA",
  "descuentoPct": 0,
  "anulado": false,
  "total": 21200.00,
  "totalCosto": 15200.00,
  "margen": 6000.00,
  "lineas": [
    {
      "productoId": 3,
      "productoNombre": "Mix Patagónico 400g",
      "unidades": 2,
      "precioUnitario": 7150.00,
      "costoUnitario": 5100.00,
      "importe": 14300.00,
      "salidaStock": 1,
      "unidadesProveedor": 1,
      "entradaStock": 0
    }
  ]
}
```

### Vista previa de precio

Para que el frontend muestre el precio al agregar la línea sin guardar:

```
GET /api/pedidos/precio    ?productoId=3&condicion=MINORISTA&descuentoPct=0
-> { "precioUnitario": 7150.00, "stockDisponible": 3 }
```

`stockDisponible` sirve para avisar en pantalla que la línea se va a pedir al
proveedor. Es informativo: el reparto real lo hace el backend al guardar.

---

## Movimientos (cobros y pagos)

```
GET    /api/movimientos                  ?desde=&hasta=&cuentaId=&tipo=
POST   /api/movimientos
DELETE /api/movimientos/{id}
```

```json
{
  "fecha": "2026-08-14",
  "tipo": "COBRO",
  "cuentaId": 12,
  "importe": 21200.00,
  "observacion": "Transferencia"
}
```

A diferencia de los pedidos, los movimientos **sí se pueden borrar**: son registros
simples sin efectos derivados sobre stock.

Un `COBRO`/`PAGO` cuyo `importe` supere el saldo actual (deuda) de la cuenta devuelve
`409`.

---

## Stock

```
GET /api/stock                           ?soloConStock=false
GET /api/stock/{productoId}/movimientos  ?desde=&hasta=
```

Listado:

```json
[
  {
    "productoId": 3,
    "nombre": "Mix Patagónico 400g",
    "categoria": "Mix 400g",
    "entradas": 5,
    "salidas": 3,
    "stockActual": 2,
    "ultimaEntrada": "2026-07-29",
    "ultimaSalida": "2026-08-05"
  }
]
```

Detalle de movimientos: fecha, número de pedido, cuenta, entrada, salida y saldo
corriendo.

---

## Cuentas corrientes

```
GET /api/cuentas-corrientes/{cuentaId}   ?desde=&hasta=
```

```json
{
  "cuenta": { "id": 12, "nombre": "Regina Quintanilla", "tipo": "CLIENTE" },
  "saldoAnterior": 0.00,
  "totalDebe": 19200.00,
  "totalHaber": 19200.00,
  "saldoFinal": 0.00,
  "lineas": [
    {
      "fecha": "2026-07-10",
      "origen": "PEDIDO",
      "detalle": "Vta 001",
      "debe": 19200.00,
      "haber": 0.00,
      "saldo": 19200.00
    },
    {
      "fecha": "2026-07-18",
      "origen": "MOVIMIENTO",
      "detalle": "Cobro",
      "debe": 0.00,
      "haber": 19200.00,
      "saldo": 0.00
    }
  ]
}
```

`saldoAnterior` es el saldo acumulado antes de `desde`. Si no se pasa `desde`, es 0.

Resumen de saldos de todas las cuentas:

```
GET /api/cuentas-corrientes/saldos       ?tipo=CLIENTE&soloConSaldo=true
-> [ { "cuentaId": 12, "nombre": "...", "saldo": 19200.00 } ]
```

---

## Remitos

```
GET /api/remitos/cliente/{pedidoId}
GET /api/remitos/proveedor               ?desde=&hasta=
```

Devuelven JSON con los datos armados. El render y la impresión son responsabilidad del
frontend. **No generar PDF en el backend.**

Remito de proveedor, consolidado por producto en todo el período (no se agrupa por
fecha):

```json
{
  "desde": "2026-08-01",
  "hasta": "2026-08-07",
  "proveedor": "Ercoli SRL",
  "totalPeriodo": 48500.00,
  "lineas": [
    {
      "producto": "Arándano 1kg",
      "unidades": 5,
      "costoUnitario": 11700.00,
      "importe": 58500.00
    }
  ]
}
```

`costoUnitario` es un promedio (`importe / unidades`): si el costo cambió entre
pedidos del período, cada línea original ya tiene su costo congelado, acá solo se
agrega.

---

## Resumen

```
GET /api/resumen                         ?desde=&hasta=
```

Sin `desde`/`hasta`, calcula sobre todos los pedidos no anulados (sin acotar por
fecha); la respuesta devuelve `desde`/`hasta` en `null` en ese caso.

```json
{
  "desde": "2026-08-01",
  "hasta": "2026-08-31",
  "ventas": 649000.00,
  "costo": 467200.00,
  "margen": 181800.00,
  "margenPct": 0.28,
  "cantidadPedidos": 34,
  "ticketPromedio": 19088.24,
  "saldoACobrar": 132400.00,
  "saldoAPagar": 88900.00
}
```
