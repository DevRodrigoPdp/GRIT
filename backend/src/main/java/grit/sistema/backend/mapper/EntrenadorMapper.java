package grit.sistema.backend.mapper;

import grit.sistema.backend.dto.entrenador.EntrenadorRequestDTO;
import grit.sistema.backend.dto.entrenador.EntrenadorResponseDTO;
import grit.sistema.backend.model.Entrenador;
import grit.sistema.backend.model.Usuario;
import grit.sistema.backend.model.enums.EstadoRevision;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EntrenadorMapper {

    public Entrenador toEntity(EntrenadorRequestDTO request, Usuario usuario, List<String> urls) {
        if (request == null || usuario == null) return null;

        Entrenador entrenador = Entrenador.builder()
                .id(usuario.getId())
                .usuario(usuario)
                .codigoProfesional(request.getCodigoProfesional())
                .titulacionEntrenamiento(request.getTitulacionEntrenamiento())
                .titulacionNutricion(request.getTitulacionNutricion())
                .documentosUrls(urls)
                .estado(EstadoRevision.PENDIENTE_REVISION)
                .build();

        // Forzamos el ID desde el usuario
        entrenador.setId(usuario.getId());

        return entrenador;
    }

    public EntrenadorResponseDTO toResponse(Entrenador entrenador) {
        if (entrenador == null) return null;

        String nombre = "N/A";
        var rol = (grit.sistema.backend.model.enums.Rol) null;

        if (entrenador.getUsuario() != null) {
            nombre = entrenador.getUsuario().getNombre();
            rol = entrenador.getUsuario().getRol();
        }

        return new EntrenadorResponseDTO(
                entrenador.getId(),
                nombre,
                rol,
                entrenador.getEstado(),
                entrenador.getTieneTituloEntrenamiento(),
                entrenador.getTieneTituloNutricion()
        );
    }
}
