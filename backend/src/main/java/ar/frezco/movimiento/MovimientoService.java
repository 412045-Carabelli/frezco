package ar.frezco.movimiento;

import ar.frezco.config.ExcepcionesNegocio;
import ar.frezco.config.Periodo;
import ar.frezco.cuenta.Cuenta;
import ar.frezco.cuenta.CuentaService;
import ar.frezco.cuentacorriente.CuentaCorrienteService;
import ar.frezco.pedido.Pedido;
import ar.frezco.pedido.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class MovimientoService {

    private final MovimientoRepository repositorio;
    private final CuentaService cuentas;
    private final CuentaCorrienteService cuentasCorrientes;
    private final PedidoRepository pedidos;

    public MovimientoService(MovimientoRepository repositorio, CuentaService cuentas,
                             CuentaCorrienteService cuentasCorrientes, PedidoRepository pedidos) {
        this.repositorio = repositorio;
        this.cuentas = cuentas;
        this.cuentasCorrientes = cuentasCorrientes;
        this.pedidos = pedidos;
    }

    public List<MovimientoDTO> listar(LocalDate desde, LocalDate hasta, Long cuentaId,
                                      TipoMovimiento tipo) {
        Periodo periodo = Periodo.de(desde, hasta);
        return repositorio.findByFechaBetweenOrderByFechaDescIdDesc(periodo.desde(), periodo.hasta())
                .stream()
                .filter(movimiento -> cuentaId == null || movimiento.getCuenta().getId().equals(cuentaId))
                .filter(movimiento -> tipo == null || movimiento.getTipo() == tipo)
                .map(MovimientoDTO::de)
                .toList();
    }

    @Transactional
    public MovimientoDTO crear(MovimientoDTO dto) {
        Cuenta cuenta = cuentas.obtener(dto.cuentaId());
        validarCombinacion(cuenta, dto.tipo());
        validarTope(cuenta, dto.importe());

        Movimiento movimiento = new Movimiento();
        movimiento.setFecha(dto.fecha());
        movimiento.setTipo(dto.tipo());
        movimiento.setCuenta(cuenta);
        movimiento.setImporte(dto.importe());
        movimiento.setObservacion(dto.observacion());
        movimiento.setPedido(resolverPedido(dto.pedidoId(), cuenta, dto.tipo()));

        return MovimientoDTO.de(repositorio.save(movimiento));
    }

    /**
     * El pedido que este movimiento salda es opcional. Si viene, mientras exista queda
     * bloqueada la edicion/anulacion de ese pedido (ver PedidoService.verificarSinPago).
     */
    private Pedido resolverPedido(Long pedidoId, Cuenta cuenta, TipoMovimiento tipo) {
        if (pedidoId == null) {
            return null;
        }
        Pedido pedido = pedidos.findById(pedidoId)
                .orElseThrow(() -> new ExcepcionesNegocio.NoEncontrado("No existe el pedido " + pedidoId));
        if (pedido.isAnulado()) {
            throw new ExcepcionesNegocio.Conflicto("El pedido " + pedido.getNumero() + " esta anulado");
        }
        if (tipo == TipoMovimiento.COBRO && !pedido.getCuenta().getId().equals(cuenta.getId())) {
            throw new ExcepcionesNegocio.Conflicto(
                    "El pedido " + pedido.getNumero() + " no es de la cuenta " + cuenta.getNombre());
        }
        return pedido;
    }

    /**
     * A diferencia de los pedidos, los movimientos si se borran: son registros simples sin
     * efectos derivados sobre el stock.
     */
    @Transactional
    public void borrar(Long id) {
        Movimiento movimiento = repositorio.findById(id)
                .orElseThrow(() -> new ExcepcionesNegocio.NoEncontrado("No existe el movimiento " + id));
        repositorio.delete(movimiento);
    }

    /** Los clientes cobran, al proveedor se le paga. Refuerzo y consumo no mueven plata. */
    private void validarCombinacion(Cuenta cuenta, TipoMovimiento tipo) {
        boolean valido = switch (cuenta.getTipo()) {
            case CLIENTE -> tipo == TipoMovimiento.COBRO;
            case PROVEEDOR -> tipo == TipoMovimiento.PAGO;
            case REFUERZO, CONSUMO -> false;
        };

        if (!valido) {
            throw new ExcepcionesNegocio.Conflicto(
                    "No se puede registrar un %s en una cuenta de tipo %s"
                            .formatted(tipo.name().toLowerCase(), cuenta.getTipo()));
        }
    }

    /** No se puede cobrar ni pagar más de lo que la cuenta debe actualmente. */
    private void validarTope(Cuenta cuenta, BigDecimal importe) {
        BigDecimal saldoActual = cuentasCorrientes.saldoActual(cuenta);
        if (importe.compareTo(saldoActual) > 0) {
            throw new ExcepcionesNegocio.Conflicto(
                    "El importe (%s) supera la deuda actual de la cuenta (%s)"
                            .formatted(importe, saldoActual));
        }
    }
}
