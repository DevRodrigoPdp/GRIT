package grit.sistema.backend.dto.entrenador;

import jakarta.validation.constraints.NotBlank;

public record RechazoDTO (
        @NotBlank(message = "El motivo es obligatorio")
        String motivo
) {
}
