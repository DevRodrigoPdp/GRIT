package grit.sistema.backend.dto.nutrition;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record PlanNutricionRequestDTO(
        @NotNull(message = "El atleta es obligatorio")
        UUID atletaId,
        @NotBlank(message = "El nombre del plan es obligatorio")
        String nombre,
        String descripcion,
        @NotNull(message = "Debe incluir al menos una comida")
        @Valid
        List<ComidaRequestDTO> comidas
) {
}
