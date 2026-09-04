package ar.frezco.cuenta;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "cuenta")
@Getter
@Setter
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoCuenta tipo;

    /** Solo informativo hasta que exista el modulo de analisis. Ver docs/07 D5. */
    @Column(length = 60)
    private String zona;

    /** Descuento por defecto que se precarga al elegir la cuenta en un pedido. */
    @Column(name = "descuento_pct", nullable = false)
    private BigDecimal descuentoPct = BigDecimal.ZERO;

    @Column(length = 30)
    private String telefono;

    @Column(length = 160)
    private String direccion;

    @Column(length = 120)
    private String email;

    @Column(nullable = false)
    private boolean activo = true;
}
