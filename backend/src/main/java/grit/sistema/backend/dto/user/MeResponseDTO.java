package grit.sistema.backend.dto.user;

import java.util.UUID;

public record MeResponseDTO (
        boolean ok,
        MeData data
){
    public record MeData (
            UUID id,
            String nombre,
            String rol,
            String estado,
            String servicio, // Solo para Atletas
            Boolean tituloEntrenamiento, // Solo para Entrenadores
            Boolean tituloNutricion,      // Solo para Entrenadores
            String titulacionEntrenamiento,
            String titulacionNutricion
    ){
    }
}
