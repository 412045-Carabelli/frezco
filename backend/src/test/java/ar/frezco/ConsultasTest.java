package ar.frezco;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Arranca solo la capa JPA contra H2. No prueba resultados: prueba que Hibernate pueda
 * parsear todos los {@code @Query} de los repositorios. Sin esto, un typo en una consulta
 * recien aparece al levantar la aplicacion contra SQL Server.
 */
@DataJpaTest
@ActiveProfiles("test")
class ConsultasTest {

    @Test
    @DisplayName("todas las consultas de los repositorios son validas")
    void lasConsultasCompilan() {
        // El contexto ya se levanto: si alguna @Query estuviera mal, este test no llegaria aca.
    }
}
