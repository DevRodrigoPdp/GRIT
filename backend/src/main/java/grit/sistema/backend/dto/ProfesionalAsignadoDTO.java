package grit.sistema.backend.dto;

import java.util.UUID;

public record ProfesionalAsignadoDTO (
        UUID id,
        String nombre,
        String titulacion,
        String rol,
        String descripcion
) {
}
