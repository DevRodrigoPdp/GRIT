package grit.sistema.backend.dto.communication;

import java.util.UUID;

public record AdjuntoDTO(
        UUID id,
        String url,
        String tipo, // "IMAGEN" | "VIDEO"
        String nombre
) {
}
