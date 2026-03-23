package com.sistema.gritfitprueba.mapper;

import com.sistema.gritfitprueba.dto.EjercicioReponseDTO;
import com.sistema.gritfitprueba.dto.EntrenamientoRequestDTO;
import com.sistema.gritfitprueba.dto.EntrenamientoResponseDTO;
import com.sistema.gritfitprueba.model.Ejercicio;
import com.sistema.gritfitprueba.model.Entrenamiento;
import com.sistema.gritfitprueba.model.Usuario;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class EntrenamientoMapper {
    public Entrenamiento toEntity(EntrenamientoRequestDTO request, Usuario usuario) {
        if(request == null) return null;

        Entrenamiento entrenamiento = new Entrenamiento();
        entrenamiento.setNombre(request.nombre());
        entrenamiento.setDescripcion(request.descripcion());
        entrenamiento.setDuracionMinutos(request.duracionMinutos());
        entrenamiento.setFechaEntrenamiento(LocalDateTime.now());
        entrenamiento.setUsuario(usuario);

        if(request.ejercicios() != null && !request.ejercicios().isEmpty()) {
            List<Ejercicio> ejercicios = request.ejercicios().stream()
                    .map(eDto -> {
                        Ejercicio ej = new Ejercicio();
                        ej.setNombre(eDto.nombre());
                        ej.setSeries(eDto.series());
                        ej.setRepeticiones(eDto.repeticiones());
                        ej.setPesoKg(eDto.pesoKg());

                        ej.setEntrenamiento(entrenamiento);
                        return ej;
                    }).toList();
            entrenamiento.setEjercicios(ejercicios);
        }

        return entrenamiento;
    }

    public EntrenamientoResponseDTO toResponse(Entrenamiento entrenamiento) {
        if(entrenamiento == null) return null;

        List<EjercicioReponseDTO> ejerciciosDto =  entrenamiento.getEjercicios().stream()
                .map(ej -> new EjercicioReponseDTO(
                        ej.getUuid().toString(),
                        ej.getNombre(),
                        ej.getSeries(),
                        ej.getRepeticiones(),
                        ej.getPesoKg()
                )).toList();

        return new EntrenamientoResponseDTO(
                entrenamiento.getUuid().toString(),
                entrenamiento.getNombre(),
                entrenamiento.getDescripcion(),
                entrenamiento.getFechaEntrenamiento(),
                entrenamiento.getDuracionMinutos(),
                ejerciciosDto
        );
    }
}
