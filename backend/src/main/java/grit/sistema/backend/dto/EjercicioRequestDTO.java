package grit.sistema.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record EjercicioRequestDTO (
    @Schema(example = "Sentadilla Búlgara")
    String nombre,

    @Schema(example = "4")
    Integer series,

    @Schema(example = "12")
    Integer repeticiones,

    @Schema(example = "15.5")
    Double pesoKg){
}
