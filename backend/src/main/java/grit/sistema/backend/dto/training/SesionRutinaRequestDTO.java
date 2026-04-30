package grit.sistema.backend.dto.training;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SesionRutinaRequestDTO(
        @NotBlank(message = "El nombre de la sesión es obligatorio")
        String nombre,

        @NotNull(message = "El orden es obligatorio")
        Integer orden,

        @NotNull(message = "Debe incluir al menos un ejercicio")
        @Valid
        List<EjercicioRequestDTO> ejercicios
) {
}
