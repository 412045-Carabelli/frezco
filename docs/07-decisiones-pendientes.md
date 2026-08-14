# 07 — Decisiones pendientes

Puntos donde el requerimiento del cliente es ambiguo. Cada uno tiene una decisión
provisoria tomada para poder avanzar. **Si el cliente responde distinto, hay que
volver acá y actualizar `03-reglas-negocio.md`.**

---

## D1 — Descuento por cuenta vs. descuento por pedido

**Ambigüedad:** el documento del cliente pide un descuento configurable por cliente y
también un campo de descuento manual en el pedido. No aclara cuál manda.

**Decisión provisoria:** el descuento de la cuenta se precarga como valor por defecto
al elegirla, y la usuaria lo puede sobrescribir en el pedido. No se suman. Una vez
guardado, queda fijo en el pedido.

**Confirmado con el cliente:** ❌ pendiente

**Impacto si cambia:** bajo. Es un campo del formulario y una línea del cálculo.

---

## D2 — Consumo propio y la deuda con el proveedor

**Ambigüedad:** el documento dice que Consumo Propio "impacta en stock existente y
cuenta corriente del proveedor". La planilla actual, en cambio, nunca genera
movimiento de proveedor para consumos: siempre descuenta de stock, aun cuando el stock
no alcanza.

**Decisión provisoria:** el consumo se comporta igual que una venta en cuanto al
reparto. Toma de stock lo que hay y el resto se le pide al proveedor. Nunca cuenta
como venta. Es la lectura que coincide con el documento escrito y la que mantiene una
sola regla para todas las salidas.

**Confirmado con el cliente:** ❌ pendiente

**Impacto si cambia:** bajo. Es una rama del algoritmo de reparto y un caso de test.

---

## D3 — Precio "por cantidad"

**Ambigüedad:** el documento lista "precio x cantidad" como una tercera condición, sin
explicar si se aplica manualmente o si se dispara automáticamente al superar cierta
cantidad de unidades.

**Decisión provisoria:** es una condición manual más, igual que mayorista. La usuaria
la elige en el pedido. No hay umbral automático.

**Confirmado con el cliente:** ❌ pendiente

**Impacto si cambia:** medio. Un umbral automático implicaría reglas por producto y
recálculo de la línea al cambiar la cantidad.

---

## D4 — Módulo de gráficas

**Ambigüedad:** el documento pide "cargar archivos multimedia para generar promociones,
páginas, flyers". No hay definición de qué genera ni cómo.

**Decisión provisoria:** fuera de alcance. No se implementa nada, ni siquiera la carga
de imágenes por producto.

**Confirmado con el cliente:** ✅ excluido explícitamente del documento de alcance

**Impacto si se define:** alto. Puede ir de 4 horas (galería de imágenes por producto)
a 30 (generador de placas con template y precio). No estimar sin definición.

---

## D5 — Zona de la cuenta

**Ambigüedad:** la planilla usa la zona para análisis de ventas, que está fuera de
alcance. No se usa para nada más.

**Decisión provisoria:** el campo se guarda pero no se usa en ningún cálculo. Queda
disponible para cuando se implemente el módulo de análisis.

**Confirmado con el cliente:** no requiere confirmación

---

## D6 — Redondeo de precios

**Ambigüedad:** en la planilla los precios finales están redondeados a mano (por
ejemplo, el cálculo da 15.399,99 y el precio cargado es 15.400).

**Decisión provisoria:** el sistema no calcula precios. Los tres precios son campos
editables que la usuaria carga con el valor final que quiere cobrar. El sistema solo
muestra, como ayuda, el margen porcentual que resulta de cada precio frente al costo.

**Confirmado con el cliente:** no requiere confirmación

**Impacto si cambia:** bajo.
