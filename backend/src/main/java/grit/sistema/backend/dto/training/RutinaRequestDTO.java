package grit.sistema.backend.dto.training;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record RutinaRequestDTO(
        @NotNull(message = "El atleta es obligatorio")
        UUID atletaId,
        @NotBlank(message = "El nombre de la rutina es obligatorio")
        String nombre,
        String descripcion,
        @NotNull(message = "La rutina debe contener sesiones")
        @Valid
        List<SesionRutinaRequestDTO> sesiones
) {
}
