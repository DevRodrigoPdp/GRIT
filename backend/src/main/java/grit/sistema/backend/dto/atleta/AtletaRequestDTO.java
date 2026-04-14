package grit.sistema.backend.dto.atleta;

import grit.sistema.backend.model.enums.NivelAtleta;
import grit.sistema.backend.model.enums.Objetivo;
import grit.sistema.backend.model.enums.TipoServicio;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record AtletaRequestDTO(
        // --- Datos de Usuario ---
        @NotBlank(message = "El nombre es obligatorio")
        @Schema(example = "Kevin García", description = "Nombre completo del usuario")
        String nombre,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Debe ser un email válido")
        @Schema(example = "kevin@gmail.com", description = "Correo electrónico que servirá como nombre de usuario")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        @Schema(example = "********", type = "string", format = "password")
        String password,

        // --- Datos de Atleta ---
        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
        LocalDate fechaNac,

        String genero,

        @DecimalMin(value = "30.0", message = "El peso mínimo es 30.0 kg")
        Double pesoKg,

        @Min(value = 100, message = "La altura mínima es 100 cm")
        Integer alturaCm,

        @NotBlank(message = "Indica qué deporte practicas")
        String deporte,

        @NotNull(message = "Debes seleccionar un nivel")
        NivelAtleta nivel,

        @NotNull(message = "Debes seleccionar un servicio")
        TipoServicio servicio,

        Objetivo objetivo
) {

}
