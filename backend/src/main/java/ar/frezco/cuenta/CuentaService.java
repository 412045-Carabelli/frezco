package ar.frezco.cuenta;

import ar.frezco.config.ExcepcionesNegocio;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class CuentaService {

    private final CuentaRepository repositorio;

    public CuentaService(CuentaRepository repositorio) {
        this.repositorio = repositorio;
    }

    public List<CuentaDTO> listar(TipoCuenta tipo, String busqueda, boolean soloActivas) {
        return repositorio.findAll(Sort.by("nombre")).stream()
                .filter(cuenta -> tipo == null || cuenta.getTipo() == tipo)
                .filter(cuenta -> busqueda == null || busqueda.isBlank()
                        || cuenta.getNombre().toLowerCase().contains(busqueda.trim().toLowerCase()))
                .filter(cuenta -> !soloActivas || cuenta.isActivo())
                .map(CuentaDTO::de)
                .toList();
    }

    public CuentaDTO buscar(Long id) {
        return CuentaDTO.de(obtener(id));
    }

    public Cuenta obtener(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new ExcepcionesNegocio.NoEncontrado("No existe la cuenta " + id));
    }

    public Cuenta obtenerPorTipo(TipoCuenta tipo) {
        return repositorio.findFirstByTipo(tipo)
                .orElseThrow(() -> new ExcepcionesNegocio.NoEncontrado(
                        "Falta la cuenta de tipo " + tipo + ", deberia haberla creado la migracion"));
    }

    @Transactional
    public CuentaDTO crear(CuentaDTO dto) {
        validarNombreLibre(dto.nombre(), null);
        if (dto.tipo().esEspecial() && repositorio.existsByTipo(dto.tipo())) {
            throw new ExcepcionesNegocio.Conflicto(
                    "Ya existe la cuenta de tipo " + dto.tipo() + " y solo puede haber una");
        }

        Cuenta cuenta = new Cuenta();
        cuenta.setTipo(dto.tipo());
        copiar(dto, cuenta);
        return CuentaDTO.de(repositorio.save(cuenta));
    }

    @Transactional
    public CuentaDTO actualizar(Long id, CuentaDTO dto) {
        validarNombreLibre(dto.nombre(), id);

        Cuenta cuenta = obtener(id);
        if (cuenta.getTipo() != dto.tipo()) {
            throw new ExcepcionesNegocio.Conflicto("No se puede cambiar el tipo de una cuenta");
        }
        if (cuenta.getTipo().esEspecial() && !dto.activo()) {
            throw new ExcepcionesNegocio.Conflicto(
                    "La cuenta " + cuenta.getNombre() + " es del sistema y no se puede dar de baja");
        }

        copiar(dto, cuenta);
        return CuentaDTO.de(repositorio.save(cuenta));
    }

    /** Baja logica. Las cuentas especiales no se dan de baja: el sistema las necesita. */
    @Transactional
    public void desactivar(Long id) {
        Cuenta cuenta = obtener(id);
        if (cuenta.getTipo().esEspecial()) {
            throw new ExcepcionesNegocio.Conflicto(
                    "La cuenta " + cuenta.getNombre() + " es del sistema y no se puede dar de baja");
        }
        cuenta.setActivo(false);
        repositorio.save(cuenta);
    }

    private void validarNombreLibre(String nombre, Long idPropio) {
        Optional<Cuenta> existente = repositorio.findByNombreIgnoreCase(nombre.trim());
        if (existente.isPresent() && !existente.get().getId().equals(idPropio)) {
            throw new ExcepcionesNegocio.Conflicto("Ya existe una cuenta con ese nombre");
        }
    }

    private void copiar(CuentaDTO dto, Cuenta cuenta) {
        cuenta.setNombre(dto.nombre().trim());
        cuenta.setZona(dto.zona());
        cuenta.setDescuentoPct(dto.descuentoPct() == null ? BigDecimal.ZERO : dto.descuentoPct());
        cuenta.setActivo(dto.activo());
    }
}
