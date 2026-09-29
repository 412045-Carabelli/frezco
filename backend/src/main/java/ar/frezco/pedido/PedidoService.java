package ar.frezco.pedido;

import ar.frezco.config.ExcepcionesNegocio;
import ar.frezco.config.Periodo;
import ar.frezco.cuenta.Cuenta;
import ar.frezco.cuenta.CuentaService;
import ar.frezco.cuenta.TipoCuenta;
import ar.frezco.pedido.precio.PoliticaPrecio;
import ar.frezco.pedido.precio.Precios;
import ar.frezco.pedido.reparto.EstrategiaReparto;
import ar.frezco.pedido.reparto.Repartos;
import ar.frezco.producto.Producto;
import ar.frezco.producto.ProductoRepository;
import ar.frezco.movimiento.MovimientoRepository;
import ar.frezco.stock.StockDisponible;
import ar.frezco.stock.StockService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PedidoService {

    private final PedidoRepository repositorio;
    private final ProductoRepository productos;
    private final CuentaService cuentas;
    private final StockService stock;
    private final Precios precios;
    private final Repartos repartos;
    private final NumeradorDePedidos numerador;
    private final MovimientoRepository movimientos;

    public PedidoService(PedidoRepository repositorio,
                         ProductoRepository productos,
                         CuentaService cuentas,
                         StockService stock,
                         Precios precios,
                         Repartos repartos,
                         NumeradorDePedidos numerador,
                         MovimientoRepository movimientos) {
        this.repositorio = repositorio;
        this.productos = productos;
        this.cuentas = cuentas;
        this.stock = stock;
        this.precios = precios;
        this.repartos = repartos;
        this.numerador = numerador;
        this.movimientos = movimientos;
    }

    public List<PedidoDTO> listar(LocalDate desde, LocalDate hasta, Long cuentaId,
                                  boolean incluirAnulados) {
        Periodo periodo = Periodo.de(desde, hasta);
        return repositorio.findByFechaBetweenOrderByFechaDescIdDesc(periodo.desde(), periodo.hasta())
                .stream()
                .filter(pedido -> incluirAnulados || !pedido.isAnulado())
                .filter(pedido -> cuentaId == null || pedido.getCuenta().getId().equals(cuentaId))
                .map(pedido -> PedidoDTO.de(pedido, movimientos.existsByPedidoId(pedido.getId())))
                .toList();
    }

    public PedidoDTO buscar(Long id) {
        Pedido pedido = obtener(id);
        return PedidoDTO.de(pedido, movimientos.existsByPedidoId(pedido.getId()));
    }

    public Pedido obtener(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new ExcepcionesNegocio.NoEncontrado("No existe el pedido " + id));
    }

    /**
     * Arma el pedido completo: resuelve precios, congela costos, reparte cada linea entre
     * stock y proveedor, y numera. Todo dentro de una transaccion.
     */
    @Transactional
    public PedidoDTO crear(CrearPedidoRequest peticion) {
        Cuenta cuenta = cuentas.obtener(peticion.cuentaId());
        if (!cuenta.isActivo()) {
            throw new ExcepcionesNegocio.Conflicto("La cuenta " + cuenta.getNombre() + " esta dada de baja");
        }

        PoliticaPrecio politica = precios.para(cuenta.getTipo());
        EstrategiaReparto reparto = repartos.para(cuenta.getTipo());

        CondicionVenta condicion = peticion.condicion() == null
                ? CondicionVenta.MINORISTA
                : peticion.condicion();
        BigDecimal descuento = politica.descuentoAplicable(
                peticion.descuentoPct() == null ? BigDecimal.ZERO : peticion.descuentoPct());

        Pedido pedido = new Pedido();
        pedido.setFecha(peticion.fecha());
        pedido.setCuenta(cuenta);
        pedido.setCondicion(condicion);
        pedido.setDescuentoPct(descuento);
        pedido.setObservacion(peticion.observacion());
        pedido.setNumero(numerador.siguiente(cuenta.getTipo()));

        StockDisponible disponible = stock.disponible();
        for (CrearPedidoRequest.LineaRequest lineaPedida : peticion.lineas()) {
            PedidoLinea linea = armarLinea(lineaPedida, condicion, descuento, politica);
            reparto.repartir(linea, disponible);
            confirmarProveedor(linea);
            pedido.agregarLinea(linea);
        }

        return PedidoDTO.de(repositorio.save(pedido), false);
    }

    /**
     * Reconstruye las lineas del pedido desde cero, igual que {@link #crear}: se recalculan
     * precio, costo y reparto stock/proveedor con los valores vigentes. La cuenta no cambia.
     * Solo se puede editar mientras el pedido no tenga cobro/pago asociado.
     */
    @Transactional
    public PedidoDTO editar(Long id, EditarPedidoRequest peticion) {
        Pedido pedido = obtener(id);
        if (pedido.isAnulado()) {
            throw new ExcepcionesNegocio.Conflicto("El pedido " + pedido.getNumero() + " esta anulado");
        }
        verificarSinPago(pedido);

        Cuenta cuenta = pedido.getCuenta();
        PoliticaPrecio politica = precios.para(cuenta.getTipo());
        EstrategiaReparto reparto = repartos.para(cuenta.getTipo());

        CondicionVenta condicion = peticion.condicion() == null
                ? CondicionVenta.MINORISTA
                : peticion.condicion();
        BigDecimal descuento = politica.descuentoAplicable(
                peticion.descuentoPct() == null ? BigDecimal.ZERO : peticion.descuentoPct());

        pedido.setFecha(peticion.fecha());
        pedido.setCondicion(condicion);
        pedido.setDescuentoPct(descuento);
        pedido.setObservacion(peticion.observacion());

        pedido.getLineas().clear();
        repositorio.flush();

        StockDisponible disponible = stock.disponible();
        for (CrearPedidoRequest.LineaRequest lineaPedida : peticion.lineas()) {
            PedidoLinea linea = armarLinea(lineaPedida, condicion, descuento, politica);
            reparto.repartir(linea, disponible);
            confirmarProveedor(linea);
            pedido.agregarLinea(linea);
        }

        return PedidoDTO.de(repositorio.save(pedido), false);
    }

    /**
     * Anulacion logica. No hay efectos derivados que escribir: stock, saldos, remitos e
     * indicadores se calculan con queries que filtran los anulados.
     */
    @Transactional
    public void anular(Long id) {
        Pedido pedido = obtener(id);
        if (pedido.isAnulado()) {
            throw new ExcepcionesNegocio.Conflicto("El pedido " + pedido.getNumero() + " ya esta anulado");
        }
        verificarSinPago(pedido);
        pedido.setAnulado(true);
        repositorio.save(pedido);
    }

    /** Una vez que el pedido tiene un cobro o pago asociado, no se puede editar ni anular. */
    private void verificarSinPago(Pedido pedido) {
        if (movimientos.existsByPedidoId(pedido.getId())) {
            throw new ExcepcionesNegocio.Conflicto(
                    "El pedido " + pedido.getNumero()
                            + " ya tiene un cobro o pago asociado y no se puede modificar");
        }
    }

    /** Precio y stock para mostrar la linea antes de guardar. */
    public PrecioPreviewDTO previsualizarPrecio(Long productoId, Long cuentaId,
                                                CondicionVenta condicion, BigDecimal descuentoPct) {
        Producto producto = obtenerProducto(productoId);
        TipoCuenta tipo = cuentaId == null ? TipoCuenta.CLIENTE : cuentas.obtener(cuentaId).getTipo();
        PoliticaPrecio politica = precios.para(tipo);

        BigDecimal descuento = politica.descuentoAplicable(
                descuentoPct == null ? BigDecimal.ZERO : descuentoPct);
        CondicionVenta condicionEfectiva = condicion == null ? CondicionVenta.MINORISTA : condicion;

        return new PrecioPreviewDTO(
                politica.precioUnitario(producto, condicionEfectiva, descuento),
                stock.deProducto(productoId));
    }

    private PedidoLinea armarLinea(CrearPedidoRequest.LineaRequest lineaPedida,
                                   CondicionVenta condicion,
                                   BigDecimal descuento,
                                   PoliticaPrecio politica) {
        Producto producto = obtenerProducto(lineaPedida.productoId());

        PedidoLinea linea = new PedidoLinea();
        linea.setProducto(producto);
        linea.setUnidades(lineaPedida.unidades());
        linea.setPrecioUnitario(politica.precioUnitario(producto, condicion, descuento));
        linea.setCostoUnitario(producto.getCosto());
        linea.setProveedor(obtenerProveedor(lineaPedida.proveedorId()));
        return linea;
    }

    private Producto obtenerProducto(Long productoId) {
        return productos.findById(productoId)
                .orElseThrow(() -> new ExcepcionesNegocio.NoEncontrado(
                        "No existe el articulo " + productoId));
    }

    private Cuenta obtenerProveedor(Long proveedorId) {
        if (proveedorId == null) {
            return null;
        }
        Cuenta cuenta = cuentas.obtener(proveedorId);
        if (cuenta.getTipo() != TipoCuenta.PROVEEDOR) {
            throw new ExcepcionesNegocio.Conflicto(cuenta.getNombre() + " no es una cuenta de proveedor");
        }
        return cuenta;
    }

    /** Recien despues de repartir se sabe si la linea le pide algo a un proveedor. */
    private void confirmarProveedor(PedidoLinea linea) {
        if (linea.getUnidadesProveedor().signum() > 0) {
            if (linea.getProveedor() == null) {
                throw new ExcepcionesNegocio.Conflicto(
                        "Elegi el proveedor para " + linea.getProducto().getNombre());
            }
        } else {
            linea.setProveedor(null);
        }
    }
}
