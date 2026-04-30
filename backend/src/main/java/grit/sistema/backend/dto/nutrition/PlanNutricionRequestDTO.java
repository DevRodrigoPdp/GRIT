package grit.sistema.backend.dto.nutrition;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;
import java.util.UUID;

public record PlanNutricionRequestDTO(
        @NotNull(message = "El atleta es obligatorio")
        UUID atletaId,

        @NotBlank(message = "El nombre del plan es obligatorio")
        @Size(max = 255)
        String nombre,

        String descripcion,

        @NotNull(message = "Las kilocalorías diarias son obligatorias")
        @Min(value = 500, message = "Un plan no puede tener menos de 500 kcal")
        Integer kcalDiarias,

        // Usamos Double o BigDecimal según lo que definiste en la entidad
        @DecimalMin(value = "0.0")
        Double proteinas,

        @DecimalMin(value = "0.0")
        Double carbos,

        @DecimalMin(value = "0.0")
        Double grasas,

        @NotNull
        Boolean activo, // Determina si se activa inmediatamente

        @NotNull(message = "Debe incluir al menos una comida")
        @Size(min = 1, message = "El plan debe tener al menos una comida")
        @Valid // CRÍTICO: Para que valide las comidas dentro de la lista
        List<ComidaRequestDTO> comidas
) {
}
