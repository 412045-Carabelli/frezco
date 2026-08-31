package ar.frezco.producto;

import ar.frezco.cuenta.Cuenta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "producto")
@Getter
@Setter
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 60)
    private String categoria;

    /** A que proveedor se le compra este articulo. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Cuenta proveedor;

    /** Kilos por unidad. Informativo. */
    private BigDecimal kg;

    /** Litros por unidad. Informativo. */
    private BigDecimal lt;

    @Column(nullable = false)
    private BigDecimal costo = BigDecimal.ZERO;

    @Column(name = "precio_minorista", nullable = false)
    private BigDecimal precioMinorista = BigDecimal.ZERO;

    @Column(name = "precio_mayorista", nullable = false)
    private BigDecimal precioMayorista = BigDecimal.ZERO;

    @Column(name = "precio_cantidad", nullable = false)
    private BigDecimal precioCantidad = BigDecimal.ZERO;

    /** Si es mayor a 0, pisa al descuento del pedido para las lineas de este articulo. */
    @Column(name = "descuento_pct", nullable = false)
    private BigDecimal descuentoPct = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean activo = true;
}
