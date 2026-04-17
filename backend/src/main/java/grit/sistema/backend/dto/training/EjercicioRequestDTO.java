package grit.sistema.backend.dto.training;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EjercicioRequestDTO(
        @NotBlank(message = "El id del ejercicio es obligatorio")
        String id,
        @NotBlank(message = "El nombre del ejercicio es obligatorio")
        String nombre,
        String categoria,
        String musculoPrincipal,
        String imagenUrl,
        @NotNull(message = "El número de series es obligatorio")
        @Min(value = 1, message = "Las series deben ser al menos 1")
        Short series,
        @NotBlank(message = "Las repeticiones son obligatorias")
        String reps,
        String notas
) {
}
