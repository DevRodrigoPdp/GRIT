package grit.sistema.backend.dto.coaching;

import grit.sistema.backend.entity.coaching.enums.TipoServicio;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AsignacionRequestDTO(
        @NotBlank(message = "El código es obligatorio")
        @Pattern(regexp = "GRIT-[A-Z0-9]{4}-[A-Z0-9]{4}", message = "Formato de código inválido")
        String codigo,

        TipoServicio rolSolicitado
) {
}
