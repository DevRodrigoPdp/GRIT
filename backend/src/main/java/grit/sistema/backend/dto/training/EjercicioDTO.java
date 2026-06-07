package grit.sistema.backend.dto.training;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record EjercicioDTO(
        UUID id,
        String nombre,
        @JsonProperty("grupoMuscular") String grupoMuscular,
        String dificultad,
        @JsonProperty("equipoNecesario") String equipoNecesario
) {
}
