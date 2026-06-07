package grit.sistema.backend.mapper.communication;

import grit.sistema.backend.dto.communication.AdjuntoDTO;
import grit.sistema.backend.dto.communication.AdjuntoData;
import grit.sistema.backend.dto.communication.CrearHiloDTO;
import grit.sistema.backend.dto.communication.MensajeDTO;
import grit.sistema.backend.entity.communication.Adjunto;
import grit.sistema.backend.entity.communication.Mensaje;
import grit.sistema.backend.service.common.StorageService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class MensajeMapper {

    @Autowired
    protected StorageService storageService;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "hilo", ignore = true)
    @Mapping(target = "enviadoEn", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "adjuntos", source = "adjuntosData")
    public abstract  Mensaje toEntity(String texto, String enviadoPor, List<AdjuntoData> adjuntosData);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "hilo", ignore = true)
    @Mapping(target = "enviadoEn", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "adjuntos", ignore = true)
    @Mapping(target = "texto", source = "dto.texto")
    @Mapping(target = "enviadoPor", source = "enviadoPor")
    public abstract  Mensaje toInitialEntity(CrearHiloDTO dto, String enviadoPor);

    @Mapping(target = "de", source = "enviadoPor")
    @Mapping(target = "fecha", source = "enviadoEn")
    public abstract MensajeDTO toDTO(Mensaje mensaje);

    @Mapping(target = "nombre", source = "nombreOriginal")
    @Mapping(target = "url", source = "s3Key", qualifiedByName = "s3KeyToUrl")
    public abstract AdjuntoDTO toAdjuntoDTO(Adjunto adjunto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mensaje", ignore = true)
    public abstract  Adjunto toAdjuntoEntity(AdjuntoData data);

    @AfterMapping
    protected void vincularRelaciones(@MappingTarget Mensaje mensaje) {
        if (mensaje.getAdjuntos() != null) {
            mensaje.getAdjuntos().forEach(a -> a.setMensaje(mensaje));
        }
    }

    @Named("s3KeyToUrl")
    protected String s3KeyToUrl(String s3Key) {
        return s3Key != null ? storageService.getPresignedUrl(s3Key) : null;
    }
}
