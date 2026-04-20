package grit.sistema.backend.model.nutrition;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "alimentos_recientes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"usuario_id", "nombre_comida", "codigo_alimento"})
})
@Getter
@Setter
public class AlimentoReciente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "nombre_comida", nullable = false, length = 100)
    private String nombreComida;

    @Column(name = "codigo_alimento", nullable = false, length = 50)
    private String codigoAlimento;

    @Column(nullable = false)
    private String nombre;

    private String marca;

    @Column(name = "kcal_por_100g", precision = 7, scale = 2)
    private java.math.BigDecimal kcalPor100g;

    @Column(name = "proteinas_por_100g", precision = 7, scale = 2)
    private java.math.BigDecimal proteinasPor100g;

    @Column(name = "carbs_por_100g", precision = 7, scale = 2)
    private java.math.BigDecimal carbsPor100g;

    @Column(name = "grasas_por_100g", precision = 7, scale = 2)
    private java.math.BigDecimal grasasPor100g;

    @Column(name = "usado_en")
    private OffsetDateTime usadoEn = OffsetDateTime.now();
}
