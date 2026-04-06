package grit.sistema.backend.mapper;

import grit.sistema.backend.dto.EntrenadorRequestDTO;
import grit.sistema.backend.dto.EntrenadorResponseDTO;
import grit.sistema.backend.model.Entrenador;
import grit.sistema.backend.model.Usuario;
import grit.sistema.backend.model.enums.EstadoRevision;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EntrenadorMapper {

    public Entrenador toEntity(EntrenadorRequestDTO request, Usuario usuario, List<String> urls){
        if (request == null) return null;

        return Entrenador.builder()
                .usuario(usuario)
                .codigoProfesional(request.codigoProfesional())
                .titulacionEntrenamiento(request.titulacionEntrenamiento())
                .titulacionNutricion(request.titulacionNutricion())
                .documentosUrls(urls)
                .estado(EstadoRevision.PENDIENTE_REVISION)
                .build();
    }

    public EntrenadorResponseDTO toResponse(Entrenador entrenador){
        if (entrenador == null) return null;

        return new EntrenadorResponseDTO(
                entrenador.getId(),
                entrenador.getUsuario().getNombre(),
                entrenador.getUsuario().getRol(),
                entrenador.getEstado(),
                entrenador.isTieneTituloEntrenamiento(),
                entrenador.isTieneTituloNutricion()
        );
    }
}
