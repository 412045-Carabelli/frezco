-- Vinculo opcional entre un movimiento (cobro/pago) y el pedido que salda. Permite
-- saber si un pedido ya se cobro/pago para bloquear su edicion. Ver docs/03-reglas-negocio.md.

ALTER TABLE movimiento ADD pedido_id BIGINT NULL;

ALTER TABLE movimiento
    ADD CONSTRAINT fk_movimiento_pedido FOREIGN KEY (pedido_id) REFERENCES pedido(id);

CREATE INDEX ix_movimiento_pedido ON movimiento(pedido_id);
