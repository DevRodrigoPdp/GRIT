package grit.sistema.backend.model.nutrition;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "alimentos_en_comida")
@Getter
@Setter
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
}
