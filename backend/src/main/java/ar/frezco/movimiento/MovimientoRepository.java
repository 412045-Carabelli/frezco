package ar.frezco.movimiento;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    List<Movimiento> findByCuentaIdOrderByFechaAscIdAsc(Long cuentaId);

    List<Movimiento> findByFechaBetweenOrderByFechaDescIdDesc(LocalDate desde, LocalDate hasta);
}
