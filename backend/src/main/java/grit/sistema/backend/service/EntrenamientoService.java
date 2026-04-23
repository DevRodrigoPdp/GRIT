package grit.sistema.backend.service;

import grit.sistema.backend.dto.training.EjercicioResponseDTO;
import grit.sistema.backend.dto.training.RutinaDTO;
import grit.sistema.backend.dto.training.RutinaRequestDTO;
import grit.sistema.backend.dto.training.RutinaResponseDTO;
import grit.sistema.backend.mapper.EntrenamientoMapper;
import grit.sistema.backend.model.training.EjercicioEnSesion;
import grit.sistema.backend.model.training.Rutina;
import grit.sistema.backend.model.training.SesionRutina;
import grit.sistema.backend.repository.AtletaRepository;
import grit.sistema.backend.repository.EntrenadorRepository;
import grit.sistema.backend.repository.RutinaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EntrenamientoService {
    private final RutinaRepository rutinaRepository;
    private final AtletaRepository atletaRepository;
    private final EntrenadorRepository entrenadorRepository;
    private final EntrenamientoMapper mapper;

    @Transactional
    public RutinaResponseDTO crearRutina(UUID entrenadorId, RutinaRequestDTO request) {
        var atleta = atletaRepository.findById(request.atletaId())
                .orElseThrow(() -> new EntityNotFoundException("Atleta no encontrado"));
        var entrenador = entrenadorRepository.findById(entrenadorId)
                .orElseThrow(() -> new EntityNotFoundException("Entrenador no encontrado"));

        // 1. MapStruct convierte el DTO en Entidad
        Rutina rutina = mapper.toEntity(request);

        // 2. Vinculamos las relaciones core
        rutina.setAtleta(atleta);
        rutina.setEntrenador(entrenador);

        // 3. Sincronización bidireccional (Crucial para JPA)
        rutina.getSesiones().forEach(sesion -> {
            sesion.setRutina(rutina);
            sesion.getEjercicios().forEach(ejercicio -> ejercicio.setSesion(sesion));
        });

        return mapper.toResponseDTO(rutinaRepository.save(rutina));
    }

    @Transactional(readOnly = true)
    public List<RutinaDTO> listarRutinas(UUID entrenadorId, UUID atletaId) {
        List<Rutina> rutinas = (atletaId == null)
                ? rutinaRepository.findAllByEntrenadorId(entrenadorId)
                : rutinaRepository.findAllByEntrenadorIdAndAtletaId(entrenadorId, atletaId);

        return rutinas.stream().map(mapper::toDTO).toList();
    }

    @Transactional
    public void eliminarRutina(UUID entrenadorId, UUID rutinaId) {
        // 1. Buscamos la rutina.
        // Si tu repositorio ya filtra por entrenadorId, la validación posterior es doble seguridad.
        Rutina rutina = rutinaRepository.findById(rutinaId)
                .orElseThrow(() -> new EntityNotFoundException("Rutina no encontrada"));

        // 2. CORRECCIÓN: Comparamos el ID del objeto entrenador con el UUID recibido
        if (!rutina.getEntrenador().getId().equals(entrenadorId)) {
            throw new AccessDeniedException("No tienes permiso para eliminar esta rutina");
        }

        // 3. Borrado físico (o podrías implementar soft-delete en el futuro)
        rutinaRepository.delete(rutina);
    }
}
