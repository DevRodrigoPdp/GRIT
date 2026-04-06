package grit.sistema.backend.dto;

import grit.sistema.backend.model.enums.TitulacionEntrenamiento;
import grit.sistema.backend.model.enums.TitulacionNutricion;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntrenadorRequestDTO {
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 3, message = "El nombre debe tener al menos 3 caracteres")
        @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$", message = "El nombre solo puede contener letras y espacios")
        private String nombre;

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Formato de email inválido")
        private String email;

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        private String password;

        private String codigoProfesional;

        private TitulacionEntrenamiento titulacionEntrenamiento;

        private TitulacionNutricion titulacionNutricion;

        @NotEmpty(message = "Debes adjuntar al menos un documento de identidad o titulación")
        private List<MultipartFile> documentos;
}
