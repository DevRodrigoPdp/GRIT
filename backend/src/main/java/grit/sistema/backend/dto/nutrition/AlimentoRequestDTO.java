package grit.sistema.backend.dto.nutrition;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record AlimentoRequestDTO(
        @NotBlank(message = "El nombre del alimento es obligatorio")
        String nombre,

        @NotNull @DecimalMin("0.1")
        Double cantidadG, // Gramos definidos por el entrenador

        // Estos campos son opcionales según tu requerimiento
        // Pero si vienen, se guardan por 100g para poder recalcular
        Double kcalPor100g,
        Double proteinasPor100g,
        Double carbsPor100g,
        Double grasasPor100g,

        String marca
) {
}
