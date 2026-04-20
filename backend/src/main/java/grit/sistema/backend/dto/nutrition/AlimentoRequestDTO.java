package grit.sistema.backend.dto.nutrition;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AlimentoRequestDTO(
        String codigo,
        @NotBlank(message = "El nombre del alimento es obligatorio")
        String nombre,
        String marca,
        @NotNull(message = "Las calorías por 100g son obligatorias")
        @DecimalMin(value = "0.0", inclusive = false, message = "Las calorías deben ser mayores que cero")
        BigDecimal kcalPor100g,
        @NotNull(message = "Las proteínas por 100g son obligatorias")
        BigDecimal proteinasPor100g,
        @NotNull(message = "Los carbohidratos por 100g son obligatorios")
        BigDecimal carbsPor100g,
        @NotNull(message = "Las grasas por 100g son obligatorias")
        BigDecimal grasasPor100g,
        @NotNull(message = "La cantidad en gramos es obligatoria")
        BigDecimal cantidadG
) {
}
