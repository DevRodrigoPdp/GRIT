package api.alimentos.model;

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
    @Column(nullable = false, length = 255)
    private String nombre;

    @Column(length = 255)
    private String marca;

    @NotNull(message = "Las calorías por 100g son obligatorias")
    @DecimalMin(value = "0.0", inclusive = true, message = "Las calorías deben ser >= 0")
    @Column(name = "kcal_por_100g", precision = 7, scale = 2, nullable = false)
    private BigDecimal kcalPor100g;

    @NotNull(message = "Las proteínas por 100g son obligatorias")
    @DecimalMin(value = "0.0", inclusive = true, message = "Las proteínas deben ser >= 0")
    @Column(name = "proteinas_por_100g", precision = 7, scale = 2, nullable = false)
    private BigDecimal proteinasPor100g;

    @NotNull(message = "Los carbohidratos por 100g son obligatorios")
    @DecimalMin(value = "0.0", inclusive = true, message = "Los carbohidratos deben ser >= 0")
    @Column(name = "carbs_por_100g", precision = 7, scale = 2, nullable = false)
    private BigDecimal carbsPor100g;

    @NotNull(message = "Las grasas por 100g son obligatorias")
    @DecimalMin(value = "0.0", inclusive = true, message = "Las grasas deben ser >= 0")
    @Column(name = "grasas_por_100g", precision = 7, scale = 2, nullable = false)
    private BigDecimal grasasPor100g;

    @Column(name = "creado_por")
    private UUID creadoPor;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private ZonedDateTime creadoEn;

    @PrePersist
    protected void onCreate() {
        if (creadoEn == null) {
            creadoEn = ZonedDateTime.now();
        }
    }
}
