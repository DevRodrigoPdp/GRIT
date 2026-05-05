package grit.sistema.backend.dto.communication;

import grit.sistema.backend.entity.communication.enums.CategoriaHilo;
import grit.sistema.backend.entity.communication.enums.ContextoHilo;

import java.time.LocalDateTime;
import java.util.UUID;

public record HiloResumenDTO(
        UUID id,
        String titulo,
        CategoriaHilo categoria,
        ContextoHilo contexto,
        String de, // "ENTRENADOR" | "ATLETA"
        LocalDateTime fechaAbierto,
        Long totalMensajes,
        UltimoMensajeDTO ultimoMensaje,
        boolean leidoPorMi
) {
}
