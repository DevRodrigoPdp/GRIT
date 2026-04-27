package grit.sistema.backend.dto.entrenador;

import grit.sistema.backend.entity.coaching.enums.TitulacionEntrenamiento;
import grit.sistema.backend.entity.coaching.enums.TitulacionNutricion;
import grit.sistema.backend.validator.ValidEntrenadorProfesional;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ValidEntrenadorProfesional
public class EntrenadorRequestDTO {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener al menos 3 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$", message = "El nombre solo puede contener letras y espacios")
    @Schema(example = "Kevin García", description = "Nombre completo del usuario")
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    @Schema(example = "kevin@gmail.com", description = "Correo electrónico que servirá como nombre de usuario")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).{8,}$",
            message = "La contraseña debe ser compleja (8+ caracteres, mayúscula, minúscula, número y símbolo)"
    )
    @Schema(example = "********", type = "string", format = "password")
    private String password;

    @Pattern(
            regexp = "^[a-zA-Z0-9.\\-/]{4,25}$",
            message = "El código profesional debe ser alfanumérico (puede incluir '.', '-' o '/') y tener entre 4 y 25 caracteres"
    )
    @Schema(description = "Identificador profesional (COLEF, NSCA, Cédula, etc.)", example = "MU-12345")
    private String codigoProfesional;

    private TitulacionEntrenamiento titulacionEntrenamiento;

    private TitulacionNutricion titulacionNutricion;

    @Min(value = 0, message = "Los años de experiencia no pueden ser negativos")
    @Max(value = 50, message = "Años de experiencia fuera de rango")
    @Schema(example = "5", description = "Años de experiencia en el sector")
    private Short experienciaAnos;

    @Size(max = 2000, message = "La descripción no puede superar los 2000 caracteres")
    @Schema(example = "Especialista en entrenamiento de fuerza y rehabilitación...", description = "Breve biografía profesional")
    private String descripcion;
}
