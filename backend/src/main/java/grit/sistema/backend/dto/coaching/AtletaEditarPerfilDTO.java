package grit.sistema.backend.dto.coaching;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;

public record AtletaEditarPerfilDTO(
        @NotBlank(message = "Indica qué deporte practicas")
        String deporte,

        @NotNull(message = "La altura es obligatoria")
        @Range(min = 100, max = 250, message = "Altura fuera de rango (100-250 cm)")
        Integer altura,

        @NotNull(message = "Debes seleccionar un nivel")
        String nivel,

        String objetivo,

        @NotNull(message = "El peso es obligatorio")
        @DecimalMin(value = "30.0", message = "Peso demasiado bajo")
        @DecimalMax(value = "300.0", message = "Peso fuera de rango profesional")
        Integer peso,
        String restriccionesDieteticas,
        String restriccionesFisicas
) {
}
