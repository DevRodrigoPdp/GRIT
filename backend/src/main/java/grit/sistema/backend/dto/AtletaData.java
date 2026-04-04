package grit.sistema.backend.dto;

import java.util.UUID;

public record AtletaData (
        UUID id,
        String estado,
        String rol
){
}
