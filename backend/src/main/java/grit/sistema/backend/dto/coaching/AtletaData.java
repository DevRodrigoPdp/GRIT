package grit.sistema.backend.dto.coaching;

import java.util.UUID;

public record AtletaData (
        UUID id,
        String estado,
        String rol
){
}
