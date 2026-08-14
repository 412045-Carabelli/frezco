package ar.frezco.pedido;

import ar.frezco.producto.Producto;
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

/**
 * El precio y el costo se congelan al guardar y no se recalculan nunca. El reparto entre
 * stock y proveedor tambien: depende del stock disponible en ese momento.
 * Ver docs/02-modelo-datos.md.
 */
@Entity
@Table(name = "pedido_linea")
@Getter
@Setter
public class PedidoLinea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private BigDecimal unidades;

    /** Congelado, ya con el descuento del pedido aplicado. */
    @Column(name = "precio_unitario", nullable = false)
    private BigDecimal precioUnitario;

    /** Congelado al momento de la carga. */
    @Column(name = "costo_unitario", nullable = false)
    private BigDecimal costoUnitario;

    @Column(name = "entrada_stock", nullable = false)
    private BigDecimal entradaStock = BigDecimal.ZERO;

    @Column(name = "salida_stock", nullable = false)
    private BigDecimal salidaStock = BigDecimal.ZERO;

    @Column(name = "unidades_proveedor", nullable = false)
    private BigDecimal unidadesProveedor = BigDecimal.ZERO;
}
