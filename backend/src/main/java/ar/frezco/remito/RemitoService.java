package ar.frezco.remito;

import ar.frezco.config.ExcepcionesNegocio;
import ar.frezco.config.Periodo;
import ar.frezco.cuenta.CuentaService;
import ar.frezco.cuenta.TipoCuenta;
import ar.frezco.pedido.Pedido;
import ar.frezco.pedido.PedidoLinea;
import ar.frezco.pedido.PedidoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class RemitoService {

    private final RemitoRepository repositorio;
    private final PedidoService pedidos;
    private final CuentaService cuentas;

    public RemitoService(RemitoRepository repositorio, PedidoService pedidos, CuentaService cuentas) {
        this.repositorio = repositorio;
        this.pedidos = pedidos;
        this.cuentas = cuentas;
    }

    /** Remito de cliente: solo para pedidos de venta. Refuerzo y consumo no generan remito. */
    public RemitoClienteDTO deCliente(Long pedidoId) {
        Pedido pedido = pedidos.obtener(pedidoId);

        if (pedido.getCuenta().getTipo() != TipoCuenta.CLIENTE) {
            throw new ExcepcionesNegocio.Conflicto(
                    "El pedido " + pedido.getNumero() + " no es una venta y no genera remito de cliente");
        }
        if (pedido.isAnulado()) {
            throw new ExcepcionesNegocio.Conflicto("El pedido " + pedido.getNumero() + " esta anulado");
        }

        List<RemitoClienteDTO.LineaDTO> lineas = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (PedidoLinea linea : pedido.getLineas()) {
            BigDecimal importe = linea.getUnidades().multiply(linea.getPrecioUnitario());
            total = total.add(importe);
            lineas.add(new RemitoClienteDTO.LineaDTO(
                    linea.getProducto().getNombre(), linea.getUnidades(),
                    linea.getPrecioUnitario(), importe));
        }

        return new RemitoClienteDTO(pedido.getId(), pedido.getNumero(), pedido.getFecha(),
                pedido.getCuenta().getNombre(), pedido.getObservacion(), total, lineas);
    }

    /** Remito de proveedor: por periodo, agrupado por fecha, con subtotal por dia. */
    public RemitoProveedorDTO deProveedor(LocalDate desde, LocalDate hasta) {
        Periodo periodo = Periodo.de(desde, hasta);

        Map<LocalDate, List<RemitoProveedorDTO.LineaDTO>> porFecha = new LinkedHashMap<>();
        BigDecimal totalPeriodo = BigDecimal.ZERO;

        for (RemitoRepository.LineaProveedor fila
                : repositorio.lineasParaProveedor(periodo.desde(), periodo.hasta())) {
            BigDecimal importe = fila.getUnidades().multiply(fila.getCostoUnitario());
            totalPeriodo = totalPeriodo.add(importe);
            porFecha.computeIfAbsent(fila.getFecha(), fecha -> new ArrayList<>())
                    .add(new RemitoProveedorDTO.LineaDTO(fila.getProducto(), fila.getUnidades(),
                            fila.getCostoUnitario(), importe));
        }

        List<RemitoProveedorDTO.DiaDTO> dias = porFecha.entrySet().stream()
                .map(entrada -> new RemitoProveedorDTO.DiaDTO(
                        entrada.getKey(),
                        entrada.getValue().stream()
                                .map(RemitoProveedorDTO.LineaDTO::importe)
                                .reduce(BigDecimal.ZERO, BigDecimal::add),
                        entrada.getValue()))
                .toList();

        return new RemitoProveedorDTO(periodo.desde(), periodo.hasta(),
                cuentas.obtenerPorTipo(TipoCuenta.PROVEEDOR).getNombre(), totalPeriodo, dias);
    }
}
