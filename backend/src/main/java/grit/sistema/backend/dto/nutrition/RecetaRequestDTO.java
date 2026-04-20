package grit.sistema.backend.dto.nutrition;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record RecetaRequestDTO(
        @NotBlank(message = "El nombre de la receta es obligatorio")
        String nombre,
        @NotNull(message = "El peso total es obligatorio")
        @DecimalMin(value = "0.1", inclusive = false, message = "El peso total debe ser mayor que cero")
        BigDecimal gramosTotal,
        @NotNull(message = "La receta debe contener ingredientes")
        @Valid
        List<IngredienteRecetaRequestDTO> ingredientes
) {
}
