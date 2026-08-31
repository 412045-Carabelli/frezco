package ar.frezco.remito;

import ar.frezco.config.ExcepcionesNegocio;
import ar.frezco.config.Periodo;
import ar.frezco.cuenta.Cuenta;
import ar.frezco.cuenta.CuentaService;
import ar.frezco.cuenta.TipoCuenta;
import ar.frezco.pedido.Pedido;
import ar.frezco.pedido.PedidoLinea;
import ar.frezco.pedido.PedidoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    /** Remito de un proveedor puntual: por periodo, consolidado por producto (sin separar por dia). */
    public RemitoProveedorDTO deProveedor(LocalDate desde, LocalDate hasta, Long proveedorId) {
        Periodo periodo = Periodo.de(desde, hasta);

        Cuenta proveedor = cuentas.obtener(proveedorId);
        if (proveedor.getTipo() != TipoCuenta.PROVEEDOR) {
            throw new ExcepcionesNegocio.Conflicto(proveedor.getNombre() + " no es una cuenta de proveedor");
        }

        List<RemitoProveedorDTO.LineaDTO> lineas = new ArrayList<>();
        BigDecimal totalPeriodo = BigDecimal.ZERO;

        for (RemitoRepository.LineaConsolidadaProveedor fila
                : repositorio.lineasConsolidadasParaProveedor(proveedorId, periodo.desde(), periodo.hasta())) {
            BigDecimal importe = fila.getImporte();
            BigDecimal costoUnitario = importe.divide(fila.getUnidades(), 2, RoundingMode.HALF_UP);
            totalPeriodo = totalPeriodo.add(importe);
            lineas.add(new RemitoProveedorDTO.LineaDTO(fila.getProducto(), fila.getUnidades(),
                    costoUnitario, importe));
        }

        return new RemitoProveedorDTO(periodo.desde(), periodo.hasta(),
                proveedor.getNombre(), totalPeriodo, lineas);
    }
}
