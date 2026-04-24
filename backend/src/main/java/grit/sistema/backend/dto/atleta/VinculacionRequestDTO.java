package grit.sistema.backend.dto.atleta;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VinculacionRequestDTO (
        @NotBlank(message = "El código es obligatorio")
        @Pattern(regexp = "GRIT-[A-Z0-9]{4}-[A-Z0-9]{4}", message = "Formato de código inválido")
        String codigo
) {
}
