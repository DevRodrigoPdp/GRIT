package grit.sistema.backend.dto.nutrition;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NotaNutricionistaRequestDTO(
        @NotBlank @Size(max = 1000) String texto
) {
}
