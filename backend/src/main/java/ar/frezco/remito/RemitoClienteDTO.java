package ar.frezco.remito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RemitoClienteDTO(
        Long pedidoId,
        String numero,
        LocalDate fecha,
        String cliente,
        String observacion,
        BigDecimal total,
        List<LineaDTO> lineas) {

    public record LineaDTO(
            String producto,
            BigDecimal unidades,
            BigDecimal precioUnitario,
            BigDecimal importe) {
    }
}
