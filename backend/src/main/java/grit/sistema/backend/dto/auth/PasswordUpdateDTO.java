package grit.sistema.backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordUpdateDTO(
        @NotBlank(message = "La actual contraseña es obligatoria") String actual,
        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).{8,}$",
                message = "La contraseña debe ser compleja (8+ caracteres, mayúscula, minúscula, número y símbolo)"
        )
        @Schema(example = "********", type = "string", format = "password")
        String nueva
) {
}
