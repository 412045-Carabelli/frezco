package ar.frezco.producto;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByActivoTrueOrderByNombreAsc();

    List<Producto> findByNombreContainingIgnoreCaseOrderByNombreAsc(String nombre);

    Optional<Producto> findByNombreIgnoreCase(String nombre);
}
