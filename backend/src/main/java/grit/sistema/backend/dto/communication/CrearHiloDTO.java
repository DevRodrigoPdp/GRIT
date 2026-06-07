package grit.sistema.backend.dto.communication;

import grit.sistema.backend.entity.communication.enums.CategoriaHilo;
import grit.sistema.backend.entity.communication.enums.ContextoHilo;

import java.util.UUID;

public record CrearHiloDTO (
        UUID atletaId,
        String titulo,
        CategoriaHilo categoria,
        ContextoHilo contexto,
        String texto
) {
}
