package grit.sistema.backend.model.nutrition;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Entity
@Table(name = "alimentos_en_comida")
@Getter
@Setter
@NoArgsConstructor
public class AlimentoEnComida {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comida_id", nullable = false)
    @NotNull(message = "El alimento debe pertenecer a una comida")
    private Comida comida;

    @Column(name = "codigo_alimento", length = 50)
    private String codigoAlimento;

    @NotNull
    @Column(nullable = false)
    private String nombre;

    private String marca;

    @NotNull
    @Column(name = "kcal_por_100g", precision = 7, scale = 2, nullable = false)
    private BigDecimal kcalPor100g;

    @NotNull
    @Column(name = "proteinas_por_100g", precision = 7, scale = 2, nullable = false)
    private BigDecimal proteinasPor100g;

    @NotNull
    @Column(name = "carbs_por_100g", precision = 7, scale = 2, nullable = false)
    private BigDecimal carbsPor100g;

    @NotNull
    @Column(name = "grasas_por_100g", precision = 7, scale = 2, nullable = false)
    private BigDecimal grasasPor100g;

    @NotNull
    @Column(name = "cantidad_g", precision = 7, scale = 2, nullable = false)
    private BigDecimal cantidadG;

    // --- MÉTODOS DE CÁLCULO (Lógica de Negocio) ---

    @Transient
    public BigDecimal getKcalTotales() {
        return calcularMacro(kcalPor100g);
    }

    @Transient
    public BigDecimal getProteinasTotales() {
        return calcularMacro(proteinasPor100g);
    }

    @Transient
    public BigDecimal getCarbsTotales() {
        return calcularMacro(carbsPor100g);
    }

    @Transient
    public BigDecimal getGrasasTotales() {
        return calcularMacro(grasasPor100g);
    }

    /**
     * Fórmula: (Valor / 100) * Cantidad Seleccionada
     */
    private BigDecimal calcularMacro(BigDecimal valorPor100g) {
        if (valorPor100g == null || cantidadG == null) return BigDecimal.ZERO;
        return valorPor100g
                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)
                .multiply(cantidadG)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
