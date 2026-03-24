package grit.sistema.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record EjercicioReponseDTO(
        @Schema(example = "550e8400-e29b-41d4-a716-446655440000")
        String uuid,

        @Schema(example = "Press de Banca")
        String nombre,

        @Schema(example = "4")
        Integer series,

        @Schema(example = "10")
        Integer repeticiones,

        @Schema(example = "60.5")
        Double pesoKg) {
}
