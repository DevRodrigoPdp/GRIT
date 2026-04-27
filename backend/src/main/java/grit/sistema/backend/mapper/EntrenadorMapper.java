package grit.sistema.backend.mapper;

import grit.sistema.backend.dto.coaching.EntrenadorRequestDTO;
import grit.sistema.backend.dto.coaching.EntrenadorResponseDTO;
import grit.sistema.backend.entity.coaching.DocumentoEntrenador;
import grit.sistema.backend.entity.coaching.Entrenador;
import grit.sistema.backend.entity.coaching.enums.DocStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EntrenadorMapper {

    @Mapping(target = "documentos", expression = "java(mapFilesToDocumentos(certificaciones, urls))")
    @Mapping(target = "estadoRevision", constant = "PENDIENTE_REVISION")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "rol", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Entrenador toEntity(EntrenadorRequestDTO request, List<String> urls, List<MultipartFile> certificaciones);

    // 2. De Entidad a ResponseDTO (Lectura)
    @Mapping(target = "id", source = "entrenador.id")
    @Mapping(target = "nombre", source = "entrenador.nombre")
    @Mapping(target = "rol", source = "entrenador.rol")
    @Mapping(target = "estado", source = "entrenador.estadoRevision")
    @Mapping(target = "tituloEntrenamiento", source = "entrenador", qualifiedByName = "checkEntrenamiento")
    @Mapping(target = "tituloNutricion", source = "entrenador", qualifiedByName = "checkNutricion")
    EntrenadorResponseDTO toResponse(Entrenador entrenador);

    @Named("checkEntrenamiento")
    default Boolean checkEntrenamiento(Entrenador e) {
        return e.getTitulacionEntrenamiento() != null;
    }

    @Named("checkNutricion")
    default Boolean checkNutricion(Entrenador e) {
        return e.getTitulacionNutricion() != null;
    }

    // Lógica personalizada para mapear los metadatos de los archivos
    default List<DocumentoEntrenador> mapFilesToDocumentos(List<MultipartFile> files, List<String> urls) {
        if (files == null || urls == null || files.size() != urls.size()) return List.of();

        List<DocumentoEntrenador> documentos = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            String url = urls.get(i);
            DocumentoEntrenador doc = new DocumentoEntrenador();
            doc.setNombreArchivo(file.getOriginalFilename());
            doc.setUrlS3(url);
            doc.setTipoMime(file.getContentType());
            doc.setTamanyoBytes((int) file.getSize());
            doc.setStatus(DocStatus.pending);
            doc.setUploadedAt(java.time.OffsetDateTime.now());
            documentos.add(doc);
        }
        return documentos;
    }
}

