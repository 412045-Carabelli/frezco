# 08 — Guía de uso

Una página. Está pensada para imprimir y dejar al lado de la compu.

## Entrar

Usuario y clave los definiste al instalar. Si te olvidás la clave, se cambia en el
archivo `.env` del servidor y se reinicia el sistema.

## Cargar una venta

1. **Pedidos → Nuevo pedido**.
2. Elegí la fecha (viene la de hoy) y buscá el cliente por nombre.
3. Elegí minorista, mayorista o por cantidad. El descuento del cliente se completa solo,
   y lo podés cambiar.
4. Tocá **Artículo**, buscá el producto y poné las unidades. El precio se completa solo.
5. Si aparece *"se pide al proveedor"*, es porque no hay stock de ese artículo. No es un
   error: el sistema lo va a agregar al pedido del proveedor.
6. **Guardar**. Vas directo al remito para mandárselo al cliente.

**Los pedidos no se editan.** Si te equivocaste, anulalo desde el listado y cargalo de
nuevo. El stock y los saldos se acomodan solos.

## Cargar mercadería que comprás de más

Es un pedido igual que una venta, pero elegís la cuenta **Refuerzo Stock**. Entra todo a
stock y se le pide todo al proveedor. No lleva precio de venta ni descuento.

## Mercadería que consumís vos

Igual, pero con la cuenta **Consumo Propio**. Sale de stock si hay; si no hay, se le pide
al proveedor. Nunca cuenta como venta.

## Mandar el remito por WhatsApp

Desde el remito: **Imprimir** → en el diálogo del celular elegí *Guardar como PDF* →
compartir. Sin pasos raros ni programas aparte.

## Pedirle al proveedor

**Remito proveedor**, elegí el período (por ejemplo, la semana) e imprimí. Sale todo lo
que hay que pedir, agrupado por día, con el costo. Junta lo de las ventas sin stock, los
refuerzos y los consumos.

## Cobrar y pagar

**Cobros y pagos**. Elegí *Cobro* para un cliente o *Pago* para el proveedor. Al elegir
la cuenta te muestra cuánto debe, antes de que cargues el importe.

## Ver cuánto te deben

**Cuentas corrientes**. Arriba está la lista de clientes con saldo, de mayor a menor, y el
saldo del proveedor. Tocando un nombre ves el detalle.

Las cuentas *Refuerzo Stock* y *Consumo Propio* son informativas: esos importes ya están
sumados dentro del saldo del proveedor, no se deben aparte.

## Ver el stock

**Stock**. Lo que está en rojo tiene stock cero. Tocando una fila ves todos los
movimientos de ese artículo.

## Precios

**Artículos**. Los tres precios los escribís vos, con el valor final redondeado que querés
cobrar. Debajo de cada uno el sistema te muestra qué margen te queda contra el costo.

## Backup

Se hace solo, todas las noches a las 3, y se guardan los últimos 7 días.

Para restaurar (esto lo hace quien administra el servidor):

```bash
docker compose exec backup ls -lh /backups
docker compose exec backup bash -c "gunzip -c /backups/frezco_AAAAMMDD_HHMMSS.bak.gz > /backups/restaurar.bak"
docker compose exec backup sqlcmd -S sqlserver,1433 -U sa -P "$DB_PASSWORD" -C \
  -Q "RESTORE DATABASE [frezco] FROM DISK = N'/backups/restaurar.bak' WITH REPLACE"
```
