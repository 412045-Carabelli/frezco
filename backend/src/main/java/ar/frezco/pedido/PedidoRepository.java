package ar.frezco.pedido;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByAnuladoFalseOrderByFechaDescIdDesc();

    List<Pedido> findByFechaBetweenOrderByFechaDescIdDesc(LocalDate desde, LocalDate hasta);

    /**
     * Ultimo numero usado para un prefijo ('Vta', 'Ref', 'Cons'). Se resuelve dentro de la
     * transaccion de guardado; incluye los anulados para no reutilizar numeros.
     */
    @Query("SELECT MAX(p.numero) FROM Pedido p WHERE p.numero LIKE CONCAT(:prefijo, ' %')")
    String ultimoNumeroDePrefijo(@Param("prefijo") String prefijo);
}
