package ar.frezco.cuenta;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    List<Cuenta> findByActivoTrueOrderByNombreAsc();

    List<Cuenta> findByTipoOrderByNombreAsc(TipoCuenta tipo);

    Optional<Cuenta> findFirstByTipo(TipoCuenta tipo);

    Optional<Cuenta> findByNombreIgnoreCase(String nombre);

    boolean existsByTipo(TipoCuenta tipo);
}
