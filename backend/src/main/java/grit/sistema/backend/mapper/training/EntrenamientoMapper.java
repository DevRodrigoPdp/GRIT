package grit.sistema.backend.mapper.training;

import grit.sistema.backend.dto.training.*;
import grit.sistema.backend.entity.training.Ejercicio;
import grit.sistema.backend.entity.training.EjercicioEnSesion;
import grit.sistema.backend.entity.training.Rutina;
import grit.sistema.backend.entity.training.SesionRutina;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface EntrenamientoMapper {

    // --- 1. MAPEADO DE RUTINA ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "atleta", ignore = true)
    @Mapping(target = "entrenador", ignore = true)
    @Mapping(target = "creadoEn", ignore = true)
    Rutina toEntity(RutinaRequestDTO request);

    @Mapping(target = "atletaId", source = "atleta.id")
    RutinaDTO toDTO(Rutina entity);

    // --- 2. MAPEADO DE SESIÓN ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rutina", ignore = true)
    // El 'orden' ya viene en SesionRutinaRequestDTO, MapStruct lo mapeará automáticamente si coinciden los nombres
    SesionRutina toEntity(SesionRutinaRequestDTO dto);

    // --- 3. MAPEADO DE EJERCICIOS (Cambios Críticos Aquí) ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sesion", ignore = true)
    // Cambiamos 'source = id' por 'source = ejercicioId' según tu nuevo Record
    @Mapping(target = "ejercicioId", source = "ejercicioId")
    // Los campos descriptivos (nombre, imagen, etc.) deben venir del DTO si es que los guardas en la tabla de cruce
    @Mapping(target = "ejercicioNombre", source = "nombre")
    @Mapping(target = "ejercicioCategoria", source = "categoria")
    @Mapping(target = "ejercicioMusculoPrincipal", source = "musculoPrincipal")
    @Mapping(target = "ejercicioImagenUrl", source = "imagenUrl")
    // Ya NO ignoramos el orden, lo recibimos del DTO
    @Mapping(target = "orden", source = "orden")
    EjercicioEnSesion toEntity(EjercicioRequestDTO dto);

    // Convierte la entidad guardada al DTO de respuesta para el Front
    @Mapping(target = "atletaId", source = "atleta.id")
    RutinaResponseDTO toResponseDTO(Rutina rutina);

    // LA CLAVE: Une los datos que vienen del usuario (DTO) con los del catálogo (Maestro)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sesion", ignore = true)
    @Mapping(target = "ejercicioId", source = "dto.ejercicioId")
    @Mapping(target = "ejercicioNombre", source = "maestro.nombre")
    @Mapping(target = "ejercicioCategoria", source = "maestro.equipoNecesario")
    @Mapping(target = "ejercicioMusculoPrincipal", source = "maestro.grupoMuscular")
    @Mapping(target = "ejercicioImagenUrl", source = "maestro.descripcion")
    // MapStruct hará .shortValue() automáticamente si 'series' es Integer en DTO y Short en Entity
    EjercicioEnSesion toEjercicioEnSesion(EjercicioRequestDTO dto, Ejercicio maestro);

    @AfterMapping
    default void vincularRelaciones(@MappingTarget Rutina rutina) {
        if (rutina.getSesiones() != null) {
            rutina.getSesiones().forEach(sesion -> {
                sesion.setRutina(rutina);
                if (sesion.getEjercicios() != null) {
                    sesion.getEjercicios().forEach(ej -> ej.setSesion(sesion));
                }
            });
        }
    }
}