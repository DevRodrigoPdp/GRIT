package grit.sistema.backend.dto.entrenador;

import grit.sistema.backend.model.enums.TitulacionEntrenamiento;
import grit.sistema.backend.model.enums.TitulacionNutricion;
import grit.sistema.backend.validator.ValidEntrenadorProfesional;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ValidEntrenadorProfesional
public class EntrenadorRequestDTO {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, message = "El nombre debe tener al menos 3 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$", message = "El nombre solo puede contener letras y espacios")
    @Schema(example = "Kevin García", description = "Nombre completo del usuario")
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    @Schema(example = "kevin@gmail.com", description = "Correo electrónico que servirá como nombre de usuario")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Schema(example = "********", type = "string", format = "password")
    private String password;

    @Schema(description = "Obligatorio si la titulación es de Grado", example = "COL-12345")
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

    @NotEmpty(message = "Debes adjuntar al menos un documento de identidad o titulación")
    private List<MultipartFile> documentos;
}
