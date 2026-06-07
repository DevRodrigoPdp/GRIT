package grit.sistema.backend.dto.coaching;

import jakarta.validation.constraints.*;

import java.util.List;

public record EntrenadorEditarPerfilDTO(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 3, max = 100, message = "El nombre debe tener al menos 3 caracteres")
        @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$", message = "El nombre solo puede contener letras y espacios")
        String nombre,

        @Size(max = 2000, message = "La descripción no puede superar los 2000 caracteres")
        String descripcion,

        @Min(value = 0, message = "Los años de experiencia no pueden ser negativos")
        @Max(value = 50, message = "Años de experiencia fuera de rango")
        Short experienciaAnos,

        List<String> masters
) {
}
