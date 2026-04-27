package grit.sistema.backend.dto.coaching;

import jakarta.validation.constraints.NotBlank;

public record RechazoDTO (
        @NotBlank(message = "El motivo es obligatorio")
        String motivo
) {
}
