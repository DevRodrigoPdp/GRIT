package grit.sistema.backend.dto.nutrition;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record AlimentoRequestDTO(
        @NotNull(message = "El ID del alimento es obligatorio")
        UUID id,

        String codigo, // Código de barras o ID externo (opcional)

        @NotBlank(message = "El nombre del alimento es obligatorio")
        String nombre,

        String marca,

        @NotNull(message = "Las calorías son obligatorias")
        @DecimalMin(value = "0.0")
        BigDecimal kcalPor100g,

        @NotNull BigDecimal proteinasPor100g,
        @NotNull BigDecimal carbsPor100g,
        @NotNull BigDecimal grasasPor100g,

        // Cantidad en gramos que se va a consumir (Opcional en Recientes, obligatorio en Plan)
        @DecimalMin(value = "0.0", message = "La cantidad debe ser positiva")
        BigDecimal cantidadG
) {
}
