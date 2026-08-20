package ar.frezco.analisis;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AnalisisDTO(
        LocalDate desde,
        LocalDate hasta,
        List<PorProducto> porProducto,
        List<PorCliente> porCliente,
        List<PorZona> porZona) {

    public record PorProducto(Long productoId, String nombre, BigDecimal unidades, BigDecimal importe) {
    }

    public record PorCliente(Long cuentaId, String nombre, long cantidadPedidos, BigDecimal importe) {
    }

    public record PorZona(String zona, BigDecimal importe) {
    }
}
