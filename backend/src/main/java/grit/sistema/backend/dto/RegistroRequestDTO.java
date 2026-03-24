package grit.sistema.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record RegistroRequestDTO(
        @Schema(example = "Kevin García", description = "Nombre completo del usuario")
        String username,

        @Schema(example = "kevin@gritfit.com", description = "Correo electrónico que servirá como nombre de usuario")
        String email,

        @Schema(example = "Password123!", description = "Contraseña segura del usuario")
        String password
) {
}
