package grit.sistema.backend.dto.user;

import java.time.OffsetDateTime;

public record UsuarioResponseDTO(
        String id,
        String nombre,
        String email,
        String rol,
        String estado,
        OffsetDateTime registro
) {
}
