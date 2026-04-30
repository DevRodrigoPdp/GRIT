package grit.sistema.backend.dto.usuario;

import java.time.OffsetDateTime;

public record UsuarioResponseDTO(
        String idPublico,
        String nombre,
        String email,
        String rol,
        OffsetDateTime registro
) {
}
