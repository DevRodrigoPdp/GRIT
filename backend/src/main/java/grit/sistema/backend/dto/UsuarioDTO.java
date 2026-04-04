package grit.sistema.backend.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioDTO(String idPublico,
                         @NotBlank(message = "El nombre no puede estar vacío")
                         String nombre,

                         @NotBlank(message = "La contraseña es obligatoria")
                         @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
                         String password,

                         @Email(message = "El formato del email no es válido")
                         @NotBlank(message = "El email es obligatorio")
                         String email,

                         String rol
) {
}
