package grit.sistema.backend.dto.atleta;

import grit.sistema.backend.model.enums.GeneroTipo;
import grit.sistema.backend.model.enums.NivelAtleta;
import grit.sistema.backend.model.enums.Objetivo;
import grit.sistema.backend.model.enums.TipoServicio;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Range;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AtletaRequestDTO(
        // --- Datos de Usuario ---
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 3, max = 100, message = "El nombre debe tener al menos 3 caracteres")
        @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$", message = "Solo letras y espacios")
        @Schema(example = "Kevin García", description = "Nombre completo del usuario")
        String nombre,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Debe ser un email válido")
        @Schema(example = "kevin@gmail.com", description = "Correo electrónico que servirá como nombre de usuario")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).{8,}$",
                message = "La contraseña debe ser compleja (8+ caracteres, mayúscula, minúscula, número y símbolo)"
        )
        @Schema(example = "********", type = "string", format = "password")
        String password,

        // --- Datos de Atleta ---
        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
        LocalDate fechaNac,

        @NotNull(message = "Género obligatorio")
        GeneroTipo genero,

        @NotNull(message = "El peso es obligatorio")
        @DecimalMin(value = "30.0", message = "Peso demasiado bajo")
        @DecimalMax(value = "300.0", message = "Peso fuera de rango profesional")
        BigDecimal pesoKg,

        @NotNull(message = "La altura es obligatoria")
        @Range(min = 100, max = 250, message = "Altura fuera de rango (100-250 cm)")
        Integer alturaCm,

        @NotBlank(message = "Indica qué deporte practicas")
        String deporte,

        @NotNull(message = "Debes seleccionar un nivel")
        NivelAtleta nivel,

        @NotNull(message = "Debes seleccionar un servicio")
        TipoServicio servicio,

        Objetivo objetivo,

        List<String> alergias, // Spring inicializa esto como lista vacía si es null en JSON

        List<String> intolerancias,

        String codigoInvitacion
) {

}
