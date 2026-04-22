package api.alimentos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO de solicitud para crear un nuevo alimento.
 * Solo disponible para entrenadores con titulación en nutrición.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlimentoCrearRequestDTO {

    @NotBlank(message = "El nombre del alimento es obligatorio")
    private String nombre;

    private String marca;

    @NotNull(message = "Las calorías por 100g son obligatorias")
    @DecimalMin(value = "0.0", inclusive = true, message = "Las calorías deben ser >= 0")
    @JsonProperty("kcalPor100g")
    private BigDecimal kcalPor100g;

    @NotNull(message = "Las proteínas por 100g son obligatorias")
    @DecimalMin(value = "0.0", inclusive = true, message = "Las proteínas deben ser >= 0")
    @JsonProperty("proteinasPor100g")
    private BigDecimal proteinasPor100g;

    @NotNull(message = "Los carbohidratos por 100g son obligatorios")
    @DecimalMin(value = "0.0", inclusive = true, message = "Los carbohidratos deben ser >= 0")
    @JsonProperty("carbsPor100g")
    private BigDecimal carbsPor100g;

    @NotNull(message = "Las grasas por 100g son obligatorias")
    @DecimalMin(value = "0.0", inclusive = true, message = "Las grasas deben ser >= 0")
    @JsonProperty("grasasPor100g")
    private BigDecimal grasasPor100g;
}
