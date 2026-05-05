package grit.sistema.backend.dto.communication;

import grit.sistema.backend.entity.communication.enums.CategoriaHilo;
import grit.sistema.backend.entity.communication.enums.ContextoHilo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record HiloDetalleDTO(
        UUID id,
        String titulo,
        CategoriaHilo categoria,
        ContextoHilo contexto,
        String creadoPor,
        LocalDateTime fechaAbierto,
        boolean leidoPorMi,
        List<MensajeDTO> mensajes
) {
}
