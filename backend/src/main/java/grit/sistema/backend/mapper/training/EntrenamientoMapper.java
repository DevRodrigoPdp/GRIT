package grit.sistema.backend.mapper.training;

import grit.sistema.backend.dto.training.*;
import grit.sistema.backend.entity.training.EjercicioEnSesion;
import grit.sistema.backend.entity.training.Rutina;
import grit.sistema.backend.entity.training.SesionRutina;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EntrenamientoMapper {

    // --- RUTINAS ---

    // Ignoramos Atleta y Entrenador porque se asignan manualmente en el Service tras validarlos
    @Mapping(target = "atleta", ignore = true)
    @Mapping(target = "entrenador", ignore = true)
    @Mapping(target = "sesiones", source = "sesiones")
    Rutina toEntity(RutinaRequestDTO request);

    @Mapping(target = "atletaId", source = "atleta.id")
    RutinaDTO toDTO(Rutina entity);

    // --- SESIONES ---

    @Mapping(target = "rutina", ignore = true) // Evita recursividad infinita
    @Mapping(target = "ejercicios", source = "ejercicios")
    SesionRutina toEntity(SesionRutinaDTO dto);

    SesionRutinaDTO toDTO(SesionRutina entity);

    // --- EJERCICIOS EN SESIÓN ---

    @Mapping(target = "sesion", ignore = true)
    @Mapping(target = "ejercicioId", source = "id") // Mapea el ID del catálogo al campo de la tabla intermedia
    EjercicioEnSesion toEntity(EjercicioRequestDTO dto);

    @Mapping(target = "id", source = "id") // ID de la fila en la tabla intermedia
    @Mapping(target = "ejercicioId", source = "ejercicioId") // ID del catálogo original
    @Mapping(target = "ejercicioNombre", source = "ejercicioNombre")
    EjercicioResponseDTO toEjercicioResponseDTO(EjercicioEnSesion entity);

    // --- RESPUESTAS ---
    RutinaResponseDTO toResponseDTO(Rutina rutina);
}
