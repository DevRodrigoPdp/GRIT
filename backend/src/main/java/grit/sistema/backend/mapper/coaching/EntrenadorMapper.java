package grit.sistema.backend.mapper.coaching;

import grit.sistema.backend.dto.coaching.EntrenadorPerfilDTO;
import grit.sistema.backend.dto.coaching.EntrenadorRequestDTO;
import grit.sistema.backend.dto.coaching.EntrenadorResponseDTO;
import grit.sistema.backend.entity.coaching.DocumentoEntrenador;
import grit.sistema.backend.entity.coaching.Entrenador;
import grit.sistema.backend.entity.coaching.enums.DocStatus;
import grit.sistema.backend.service.common.StorageService;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class EntrenadorMapper {
    @Autowired
    protected StorageService storageService;

    // --- 1. REQUEST -> ENTITY (Registro) ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true) // El password se encripta en el Service
    @Mapping(target = "rol", ignore = true)      // El rol se asigna en el Service
    @Mapping(target = "documentos", expression = "java(mapFilesToDocumentos(certificaciones, urls))")
    @Mapping(target = "estadoRevision", constant = "PENDIENTE_REVISION")
    public abstract Entrenador toEntity(EntrenadorRequestDTO request, List<String> urls, List<MultipartFile> certificaciones);

    // --- 2. ENTITY -> RESPONSE DTO (Listados rápidos) ---
    @Mapping(target = "estado", source = "estadoRevision")
    @Mapping(target = "tituloEntrenamiento", expression = "java(entrenador.getTitulacionEntrenamiento() != null)")
    @Mapping(target = "tituloNutricion", expression = "java(entrenador.getTitulacionNutricion() != null)")
    public abstract EntrenadorResponseDTO toResponse(Entrenador entrenador);

    // --- 3. ENTITY -> PERFIL DTO (Detalle completo del perfil) ---
    @Mapping(target = "correo", source = "email")
    @Mapping(target = "estado", source = "estadoRevision", qualifiedByName = "enumToString")
    @Mapping(target = "solicitudAmpliacion", source = "solicitudAmpliacionPendiente")
    @Mapping(target = "titulacionEntrenamiento", source = "titulacionEntrenamiento", qualifiedByName = "enumToString")
    @Mapping(target = "titulacionNutricion", source = "titulacionNutricion", qualifiedByName = "enumToString")
    @Mapping(target = "fotoUrl", source = "fotoUrl", qualifiedByName = "toPresignedUrl")
    public abstract EntrenadorPerfilDTO toPerfilDTO(Entrenador entrenador);

    // --- MÉTODOS DE SOPORTE (Lógica de transformación) ---

    @Named("toPresignedUrl")
    protected String toPresignedUrl(String key) {
        if (key == null || key.isBlank()) return null;
        return storageService.getPresignedUrl(key);
    }

    @Named("enumToString")
    protected String enumToString(Enum<?> anyEnum) {
        return anyEnum != null ? anyEnum.name() : null;
    }

    protected List<DocumentoEntrenador> mapFilesToDocumentos(List<MultipartFile> files, List<String> urls) {
        if (files == null || urls == null || files.size() != urls.size()) return new ArrayList<>();

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

