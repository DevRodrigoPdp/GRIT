package com.sistema.gritfitprueba.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record EntrenamientoRequestDTO(
        @Schema(example = "Rutina de Pierna", description = "Nombre del entrenamiento")
        String nombre,

        @Schema(example = "Enfoque en cuádriceps", description = "Breve descripción")
        String descripcion,

        @Schema(example = "60")
        Integer duracionMinutos,

        @Schema(description = "Lista de ejercicios del entrenamiento")
        List<EjercicioRequestDTO> ejercicios
) {
}
