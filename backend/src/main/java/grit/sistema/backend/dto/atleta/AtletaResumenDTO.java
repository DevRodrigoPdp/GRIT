package grit.sistema.backend.dto.atleta;

import java.util.UUID;

public record AtletaResumenDTO(
        UUID id,
        String nombre,
        String deporte,
        String nivel,
        String servicio,
        boolean tienePlanActivo
) {
}
