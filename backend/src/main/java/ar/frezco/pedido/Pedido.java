package ar.frezco.pedido;

import ar.frezco.cuenta.Cuenta;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Los pedidos no se editan. Ante un error se marcan {@code anulado} y se cargan de nuevo;
 * todos los calculos ignoran los anulados.
 */
@Entity
@Table(name = "pedido")
@Getter
@Setter
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Prefijo por tipo de cuenta mas tres digitos: 'Vta 001', 'Ref 001', 'Cons 001'. */
    @Column(nullable = false, length = 20)
    private String numero;

    @Column(nullable = false)
    private LocalDate fecha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuenta_id", nullable = false)
    private Cuenta cuenta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CondicionVenta condicion;

    @Column(name = "descuento_pct", nullable = false)
    private BigDecimal descuentoPct = BigDecimal.ZERO;

    @Column(length = 300)
    private String observacion;

    @Column(nullable = false)
    private boolean anulado = false;

    // DATETIME2 en UTC, no DATETIME OFFSET: lo escribe el DEFAULT SYSUTCDATETIME() de la tabla.
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @Column(name = "creado_en", nullable = false, insertable = false, updatable = false)
    private Instant creadoEn;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoLinea> lineas = new ArrayList<>();

    public void agregarLinea(PedidoLinea linea) {
        linea.setPedido(this);
        lineas.add(linea);
    }
}
