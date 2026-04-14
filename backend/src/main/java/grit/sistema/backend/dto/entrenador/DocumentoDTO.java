package grit.sistema.backend.dto.entrenador;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentoDTO(
        UUID id,
        String nombre_archivo,
        String url_firmada,
        LocalDateTime uploaded_at
) {
}
