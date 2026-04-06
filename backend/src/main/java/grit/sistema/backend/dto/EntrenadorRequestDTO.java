package grit.sistema.backend.dto;

import grit.sistema.backend.model.enums.TitulacionEntrenamiento;
import grit.sistema.backend.model.enums.TitulacionNutricion;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record EntrenadorRequestDTO(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 3, message = "El nombre debe tener al menos 3 caracteres")
        @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$", message = "El nombre solo puede contener letras y espacios")
        String nombre,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Formato de email inválido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String password,

        // Opcional o obligatorio según validación cruzada en el Service
        String codigoProfesional,

        TitulacionEntrenamiento titulacionEntrenamiento,

        TitulacionNutricion titulacionNutricion,

        @NotEmpty(message = "Debes adjuntar al menos un documento de identidad o titulación")
        List<MultipartFile> documentos
) {
}
