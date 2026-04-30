package grit.sistema.backend.dto.nutrition;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AlimentoRecienteRequestDTO(
        @NotBlank(message = "El nombre de la comida es obligatorio (ej: Desayuno)")
        String nombreComida,

        @NotNull(message = "Los datos del alimento son obligatorios")
        @Valid
        AlimentoDTO alimento // Usamos AlimentoDTO que ya tiene la estructura calculada o de entidad
) {
}
