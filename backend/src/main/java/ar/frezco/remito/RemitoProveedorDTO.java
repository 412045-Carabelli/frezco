package ar.frezco.remito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RemitoProveedorDTO(
        LocalDate desde,
        LocalDate hasta,
        String proveedor,
        BigDecimal totalPeriodo,
        List<LineaDTO> lineas) {

    public record LineaDTO(
            String producto,
            BigDecimal unidades,
            BigDecimal costoUnitario,
            BigDecimal importe) {
    }
}
