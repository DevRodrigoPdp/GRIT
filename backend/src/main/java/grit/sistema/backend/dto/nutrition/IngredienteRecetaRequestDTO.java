package grit.sistema.backend.dto.nutrition;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record IngredienteRecetaRequestDTO(
        String codigo,
        @NotBlank(message = "El nombre del ingrediente es obligatorio")
        String nombre,
        @NotNull(message = "Las calorías del ingrediente son obligatorias")
        @DecimalMin(value = "0.0", inclusive = false, message = "Las calorías deben ser mayores que cero")
        BigDecimal kcalPor100g,
        @NotNull(message = "La cantidad en gramos del ingrediente es obligatoria")
        BigDecimal cantidadG
) {
}
