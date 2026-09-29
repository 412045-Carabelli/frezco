package ar.frezco.pedido;

import ar.frezco.cuenta.TipoCuenta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PedidoDTO(
        Long id,
        String numero,
        LocalDate fecha,
        CuentaResumenDTO cuenta,
        CondicionVenta condicion,
        BigDecimal descuentoPct,
        String observacion,
        boolean anulado,
        boolean editable,
        BigDecimal total,
        BigDecimal totalCosto,
        BigDecimal margen,
        List<PedidoLineaDTO> lineas) {

    public record CuentaResumenDTO(Long id, String nombre, TipoCuenta tipo) {
    }

    public record PedidoLineaDTO(
            Long productoId,
            String productoNombre,
            BigDecimal unidades,
            BigDecimal precioUnitario,
            BigDecimal costoUnitario,
            BigDecimal importe,
            BigDecimal entradaStock,
            BigDecimal salidaStock,
            BigDecimal unidadesProveedor,
            Long proveedorId,
            String proveedorNombre) {

        static PedidoLineaDTO de(PedidoLinea linea) {
            return new PedidoLineaDTO(
                    linea.getProducto().getId(),
                    linea.getProducto().getNombre(),
                    linea.getUnidades(),
                    linea.getPrecioUnitario(),
                    linea.getCostoUnitario(),
                    linea.getUnidades().multiply(linea.getPrecioUnitario()),
                    linea.getEntradaStock(),
                    linea.getSalidaStock(),
                    linea.getUnidadesProveedor(),
                    linea.getProveedor() == null ? null : linea.getProveedor().getId(),
                    linea.getProveedor() == null ? null : linea.getProveedor().getNombre());
        }
    }

    /** {@code tienePago}: si tiene un cobro/pago asociado, no se puede editar ni anular. */
    public static PedidoDTO de(Pedido pedido, boolean tienePago) {
        List<PedidoLineaDTO> lineas = pedido.getLineas().stream()
                .map(PedidoLineaDTO::de)
                .toList();

        BigDecimal total = lineas.stream()
                .map(PedidoLineaDTO::importe)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCosto = pedido.getLineas().stream()
                .map(linea -> linea.getUnidades().multiply(linea.getCostoUnitario()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PedidoDTO(
                pedido.getId(),
                pedido.getNumero(),
                pedido.getFecha(),
                new CuentaResumenDTO(pedido.getCuenta().getId(), pedido.getCuenta().getNombre(),
                        pedido.getCuenta().getTipo()),
                pedido.getCondicion(),
                pedido.getDescuentoPct(),
                pedido.getObservacion(),
                pedido.isAnulado(),
                !pedido.isAnulado() && !tienePago,
                total,
                totalCosto,
                total.subtract(totalCosto),
                lineas);
    }
}
