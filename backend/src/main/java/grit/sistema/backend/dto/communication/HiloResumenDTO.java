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
        String creadoPor,
        LocalDateTime creadoEn,
        Integer totalMensajes,         // CAMBIADO: De Long a Integer para resolver el conflicto
        String ultimoTexto,
        LocalDateTime ultimoEnvio,
        String ultimoEnviadoPor,
        boolean leido
) {}