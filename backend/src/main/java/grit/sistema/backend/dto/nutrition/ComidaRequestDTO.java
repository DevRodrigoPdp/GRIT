package grit.sistema.backend.dto.nutrition;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ComidaRequestDTO(
        @NotBlank(message = "El nombre de la comida es obligatorio")
        String nombre,
        @NotNull(message = "Debe incluir al menos un alimento")
        @Valid
        List<AlimentoRequestDTO> alimentos
) {
}
