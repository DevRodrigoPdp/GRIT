package grit.sistema.backend.dto.atleta;

import java.util.UUID;

public record AtletaData (
        UUID id,
        String estado,
        String rol
){
}
