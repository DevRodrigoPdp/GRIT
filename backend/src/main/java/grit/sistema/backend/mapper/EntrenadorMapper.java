package grit.sistema.backend.mapper;

import grit.sistema.backend.dto.entrenador.EntrenadorRequestDTO;
import grit.sistema.backend.dto.entrenador.EntrenadorResponseDTO;
import grit.sistema.backend.model.coaching.DocumentoEntrenador;
import grit.sistema.backend.model.coaching.Entrenador;
import grit.sistema.backend.model.Usuario;
import grit.sistema.backend.model.enums.DocStatus;
import grit.sistema.backend.model.enums.EstadoRevision;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface EntrenadorMapper {

    // 1. Mapeamos el ID (PK compartida)
    @Mapping(target = "id", source = "usuario.id")

    // 2. Mapeamos los campos heredados de Usuario
    @Mapping(target = "nombre", source = "usuario.nombre")
    @Mapping(target = "email", source = "usuario.email")
    @Mapping(target = "rol", source = "usuario.rol")
    @Mapping(target = "password", source = "usuario.password")

    // 3. Campos específicos de Entrenador (vienen del DTO)
    @Mapping(target = "codigoProfesional", source = "request.codigoProfesional")
    @Mapping(target = "titulacionEntrenamiento", source = "request.titulacionEntrenamiento")
    @Mapping(target = "titulacionNutricion", source = "request.titulacionNutricion")

    // 4. Lógica de estado y documentos
    @Mapping(target = "estado", constant = "PENDIENTE_REVISION")
    @Mapping(target = "documentos", source = "urls", qualifiedByName = "mapUrlsToDocumentos")

    // 5. Ignorar auditoría
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Entrenador toEntity(EntrenadorRequestDTO request,Usuario usuario, List<String> urls);

    @Mapping(target = "id", source = "entrenador.id")
    @Mapping(target = "nombre", source = "entrenador.usuario.nombre")
    @Mapping(target = "rol", source = "entrenador.usuario.rol")
    @Mapping(target = "estado", source = "entrenador.estado") // El estado del Entrenador, no del Usuario
    @Mapping(target = "tituloEntrenamiento", source = "entrenador", qualifiedByName = "checkEntrenamiento")
    @Mapping(target = "tituloNutricion", source = "entrenador", qualifiedByName = "checkNutricion")
    EntrenadorResponseDTO toResponse(Entrenador entrenador);

    @Named("checkEntrenamiento")
    default boolean checkEntrenamiento(Entrenador e) {
        return e.getTitulacionEntrenamiento() != null;
    }

    @Named("checkNutricion")
    default boolean checkNutricion(Entrenador e) {
        return e.getTitulacionNutricion() != null;
    }

    @Named("mapUrlsToDocumentos")
    default List<DocumentoEntrenador> mapUrlsToDocumentos(List<String> urls) {
        if (urls == null) return List.of();
        return urls.stream().map(url -> {
            DocumentoEntrenador doc = new DocumentoEntrenador();
            doc.setUrlS3(url);
            doc.setNombreArchivo(url.substring(url.lastIndexOf("/") + 1));
            doc.setStatus(DocStatus.pending);
            return doc;
        }).toList();
    }
}

