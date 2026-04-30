package grit.sistema.backend.dto.training;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EjercicioRequestDTO(
        @NotNull(message = "El id del catálogo de ejercicios es obligatorio")
        UUID ejercicioId,

        @NotBlank String nombre,           // Añadido
        String categoria,                  // Añadido
        String musculoPrincipal,           // Añadido
        String imagenUrl,

        @NotNull(message = "El orden es obligatorio")
        Integer orden,

        @NotNull(message = "El número de series es obligatorio")
        @Min(value = 1, message = "Mínimo 1 serie")
        Integer series,

        @NotBlank(message = "Las repeticiones son obligatorias")
        String reps,

        String notas
) {
}
