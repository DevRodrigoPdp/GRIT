package grit.sistema.backend.model.nutrition;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "ingredientes_receta")
@Getter
@Setter
public class IngredienteReceta {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receta_id", nullable = false)
    private Receta receta;

    @Column(name = "codigo_alimento", length = 50)
    private String codigoAlimento;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "kcal_por_100g", precision = 7, scale = 2, nullable = false)
    private BigDecimal kcalPor100g;

    @Column(name = "cantidad_g", precision = 7, scale = 2, nullable = false)
    private BigDecimal cantidadG;

    private Short orden;
}
