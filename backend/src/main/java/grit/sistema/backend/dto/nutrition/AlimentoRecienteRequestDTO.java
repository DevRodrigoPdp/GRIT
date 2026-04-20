package grit.sistema.backend.dto.nutrition;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AlimentoRecienteRequestDTO(
        @NotBlank(message = "El nombre de la comida es obligatorio")
        String nombreComida,
        @NotNull(message = "El alimento es obligatorio")
        @Valid
        AlimentoRequestDTO alimento
) {
}
