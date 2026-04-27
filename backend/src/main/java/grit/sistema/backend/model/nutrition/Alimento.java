package grit.sistema.backend.model.nutrition;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Entidad que representa un alimento en la base de datos.
 * Los alimentos pueden ser creados por el seeder (creado_por = null) o por nutricionistas.
 */
@Entity
@Table(name = "alimentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alimento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "El nombre del alimento es obligatorio")
    @Column(nullable = false)
    private String nombre;

    private String marca;

    @Column(name = "categoria") // ¡Te faltaba este campo que pusimos en el SQL!
    private String categoria;

    @NotNull
    @DecimalMin("0.0")
    @Column(name = "kcal_por_100g", precision = 7, scale = 2, nullable = false)
    private BigDecimal kcalPor100g;

    @NotNull
    @DecimalMin("0.0")
    @Column(name = "proteinas_por_100g", precision = 7, scale = 2, nullable = false)
    private BigDecimal proteinasPor100g;

    @NotNull
    @DecimalMin("0.0")
    @Column(name = "carbs_por_100g", precision = 7, scale = 2, nullable = false)
    private BigDecimal carbsPor100g;

    @NotNull
    @DecimalMin("0.0")
    @Column(name = "grasas_por_100g", precision = 7, scale = 2, nullable = false)
    private BigDecimal grasasPor100g;

    @Column(name = "creado_por")
    private UUID creadoPor;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private ZonedDateTime creadoEn;

    @Column(name = "actualizado_en")
    private ZonedDateTime actualizadoEn;

    @PrePersist
    protected void onCreate() {
        creadoEn = ZonedDateTime.now();
        actualizadoEn = ZonedDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        actualizadoEn = ZonedDateTime.now();
    }
}
