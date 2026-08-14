package ar.frezco.movimiento;

import ar.frezco.config.ExcepcionesNegocio;
import ar.frezco.config.Periodo;
import ar.frezco.cuenta.Cuenta;
import ar.frezco.cuenta.CuentaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class MovimientoService {

    private final MovimientoRepository repositorio;
    private final CuentaService cuentas;

    public MovimientoService(MovimientoRepository repositorio, CuentaService cuentas) {
        this.repositorio = repositorio;
        this.cuentas = cuentas;
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

        Movimiento movimiento = new Movimiento();
        movimiento.setFecha(dto.fecha());
        movimiento.setTipo(dto.tipo());
        movimiento.setCuenta(cuenta);
        movimiento.setImporte(dto.importe());
        movimiento.setObservacion(dto.observacion());

        return MovimientoDTO.de(repositorio.save(movimiento));
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
}
