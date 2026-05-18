package grit.sistema.backend.mapper.communication;

import grit.sistema.backend.dto.communication.CrearHiloDTO;
import grit.sistema.backend.dto.communication.HiloDetalleDTO;
import grit.sistema.backend.entity.communication.Hilo;
import grit.sistema.backend.entity.communication.Mensaje;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = { MensajeMapper.class })
public abstract class HiloMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mensajes", ignore = true)
    @Mapping(target = "atleta", ignore = true)
    @Mapping(target = "entrenador", ignore = true)
    @Mapping(target = "creadoPor", source = "emisorRol")
    @Mapping(target = "creadoEn", expression = "java(java.time.LocalDateTime.now())")
    public abstract Hilo toEntity(CrearHiloDTO dto, String emisorRol);

    @Mapping(target = "de", source = "hilo.creadoPor")
    @Mapping(target = "fechaAbierto", source = "hilo.creadoEn")
    @Mapping(target = "leidoPorMi", source = "leido")
    public abstract HiloDetalleDTO toDetalleDTO(Hilo hilo, boolean leido);

    @AfterMapping
    protected void linkInitialMessage(CrearHiloDTO dto, String emisorRol, @MappingTarget Hilo hilo, @Context MensajeMapper mensajeMapper) {
        Mensaje inicial = mensajeMapper.toInitialEntity(dto, emisorRol);
        inicial.setHilo(hilo);
        hilo.getMensajes().add(inicial);
    }
}
