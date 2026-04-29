package grit.sistema.backend.mapper.training;

import grit.sistema.backend.dto.training.*;
import grit.sistema.backend.entity.training.EjercicioEnSesion;
import grit.sistema.backend.entity.training.Rutina;
import grit.sistema.backend.entity.training.SesionRutina;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
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

    // --- 2. MAPEADO DE SESIÓN (Debe coincidir con el tipo en RutinaRequestDTO) ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rutina", ignore = true)
    @Mapping(target = "orden", ignore = true)
    // MapStruct ahora encontrará 'orden' en SesionRutinaRequestDTO
    SesionRutina toEntity(SesionRutinaRequestDTO dto);

    // --- 3. MAPEADO DE EJERCICIOS ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sesion", ignore = true)
    @Mapping(target = "ejercicioId", source = "id")
    @Mapping(target = "ejercicioNombre", source = "nombre")
    @Mapping(target = "ejercicioCategoria", source = "categoria")
    @Mapping(target = "ejercicioMusculoPrincipal", source = "musculoPrincipal")
    @Mapping(target = "ejercicioImagenUrl", source = "imagenUrl")
    @Mapping(target = "orden", ignore = true) // Ignorar si el frontend no envía orden de ejercicios
    EjercicioEnSesion toEntity(EjercicioRequestDTO dto);

    // --- 4. VINCULACIÓN JERÁRQUICA (Vital para JPA) ---
    @AfterMapping
    default void vincularRelaciones(@MappingTarget Rutina rutina) {
        if (rutina.getSesiones() != null) {
            rutina.getSesiones().forEach(sesion -> {
                sesion.setRutina(rutina); // Crucial para que JPA guarde el FK
                if (sesion.getEjercicios() != null) {
                    sesion.getEjercicios().forEach(ej -> ej.setSesion(sesion));
                }
            });
        }
    }

    RutinaResponseDTO toResponseDTO(Rutina rutina);
}