package ar.frezco.producto;

import ar.frezco.config.ExcepcionesNegocio;
import ar.frezco.cuenta.Cuenta;
import ar.frezco.cuenta.CuentaService;
import ar.frezco.cuenta.TipoCuenta;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ProductoService {

    private final ProductoRepository repositorio;
    private final CuentaService cuentas;

    public ProductoService(ProductoRepository repositorio, CuentaService cuentas) {
        this.repositorio = repositorio;
        this.cuentas = cuentas;
    }

    public List<ProductoDTO> listar(String busqueda, boolean soloActivos) {
        List<Producto> productos = (busqueda == null || busqueda.isBlank())
                ? repositorio.findAll(org.springframework.data.domain.Sort.by("nombre"))
                : repositorio.findByNombreContainingIgnoreCaseOrderByNombreAsc(busqueda.trim());

        return productos.stream()
                .filter(producto -> !soloActivos || producto.isActivo())
                .map(ProductoDTO::de)
                .toList();
    }

    public ProductoDTO buscar(Long id) {
        return ProductoDTO.de(obtener(id));
    }

    Producto obtener(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new ExcepcionesNegocio.NoEncontrado("No existe el articulo " + id));
    }

    @Transactional
    public ProductoDTO crear(ProductoDTO dto) {
        validarNombreLibre(dto.nombre(), null);

        Producto producto = new Producto();
        copiar(dto, producto);
        return ProductoDTO.de(repositorio.save(producto));
    }

    @Transactional
    public ProductoDTO actualizar(Long id, ProductoDTO dto) {
        validarNombreLibre(dto.nombre(), id);

        Producto producto = obtener(id);
        copiar(dto, producto);
        return ProductoDTO.de(repositorio.save(producto));
    }

    /** Baja logica: los pedidos historicos siguen apuntando al articulo. */
    @Transactional
    public void desactivar(Long id) {
        Producto producto = obtener(id);
        producto.setActivo(false);
        repositorio.save(producto);
    }

    private void validarNombreLibre(String nombre, Long idPropio) {
        Optional<Producto> existente = repositorio.findByNombreIgnoreCase(nombre.trim());
        if (existente.isPresent() && !existente.get().getId().equals(idPropio)) {
            throw new ExcepcionesNegocio.Conflicto("Ya existe un articulo con ese nombre");
        }
    }

    private void copiar(ProductoDTO dto, Producto producto) {
        producto.setNombre(dto.nombre().trim());
        producto.setCategoria(dto.categoria());
        producto.setProveedor(obtenerProveedor(dto.proveedorId()));
        producto.setKg(dto.kg());
        producto.setLt(dto.lt());
        producto.setCosto(valorOCero(dto.costo()));
        producto.setPrecioMinorista(valorOCero(dto.precioMinorista()));
        producto.setPrecioMayorista(valorOCero(dto.precioMayorista()));
        producto.setPrecioCantidad(valorOCero(dto.precioCantidad()));
        producto.setDescuentoPct(valorOCero(dto.descuentoPct()));
        producto.setActivo(dto.activo());
    }

    private BigDecimal valorOCero(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private Cuenta obtenerProveedor(Long proveedorId) {
        Cuenta cuenta = cuentas.obtener(proveedorId);
        if (cuenta.getTipo() != TipoCuenta.PROVEEDOR) {
            throw new ExcepcionesNegocio.Conflicto(cuenta.getNombre() + " no es una cuenta de proveedor");
        }
        return cuenta;
    }
}
