package grit.sistema.backend.service;

import grit.sistema.backend.dto.training.EjercicioResponseDTO;
import grit.sistema.backend.dto.training.RutinaDTO;
import grit.sistema.backend.dto.training.RutinaRequestDTO;
import grit.sistema.backend.dto.training.RutinaResponseDTO;
import grit.sistema.backend.model.training.EjercicioEnSesion;
import grit.sistema.backend.model.training.Rutina;
import grit.sistema.backend.model.training.SesionRutina;
import grit.sistema.backend.repository.AtletaRepository;
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

    @Transactional(readOnly = true)
    public List<RutinaDTO> listarRutinas(UUID entrenadorId, UUID atletaId) {
        List<Rutina> rutinas = atletaId == null
                ? rutinaRepository.findAllByEntrenadorId(entrenadorId)
                : rutinaRepository.findAllByEntrenadorIdAndAtletaId(entrenadorId, atletaId);

        return rutinas.stream()
                .map(this::mapToRutinaDTO)
                .toList();
    }

    @Transactional
    public RutinaResponseDTO crearRutina(UUID entrenadorId, RutinaRequestDTO request) {
        atletaRepository.findById(request.atletaId())
                .orElseThrow(() -> new EntityNotFoundException("Atleta no encontrado"));

        Rutina rutina = new Rutina();
        rutina.setEntrenadorId(entrenadorId);
        rutina.setAtletaId(request.atletaId());
        rutina.setNombre(request.nombre());
        rutina.setDescripcion(request.descripcion());
        rutina.setCreadoEn(OffsetDateTime.now());

        List<SesionRutina> sesiones = request.sesiones().stream()
                .map((sessionRequest) -> {
                    SesionRutina sesion = new SesionRutina();
                    sesion.setRutina(rutina);
                    sesion.setNombre(sessionRequest.nombre());
                    sesion.setOrden((short) (request.sesiones().indexOf(sessionRequest) + 1));
                    List<EjercicioEnSesion> ejercicios = sessionRequest.ejercicios().stream()
                            .map((ejercicioRequest) -> {
                                EjercicioEnSesion ejercicio = new EjercicioEnSesion();
                                ejercicio.setSesion(sesion);
                                ejercicio.setEjercicioId(ejercicioRequest.id());
                                ejercicio.setEjercicioNombre(ejercicioRequest.nombre());
                                ejercicio.setSeries((short) ejercicioRequest.series());
                                ejercicio.setReps(ejercicioRequest.reps());
                                ejercicio.setNotas(ejercicioRequest.notas());
                                ejercicio.setOrden((short) (sessionRequest.ejercicios().indexOf(ejercicioRequest) + 1));
                                return ejercicio;
                            })
                            .toList();
                    sesion.setEjercicios(ejercicios);
                    return sesion;
                })
                .toList();

        rutina.setSesiones(sesiones);
        Rutina guardada = rutinaRepository.save(rutina);
        return new RutinaResponseDTO(guardada.getId(), guardada.getCreadoEn());
    }

    @Transactional
    public void eliminarRutina(UUID entrenadorId, UUID rutinaId) {
        Rutina rutina = rutinaRepository.findByIdAndEntrenadorId(rutinaId, entrenadorId)
                .orElseThrow(() -> new EntityNotFoundException("Rutina no encontrada"));

        if (!rutina.getEntrenadorId().equals(entrenadorId)) {
            throw new AccessDeniedException("No tienes permiso para eliminar esta rutina");
        }

        rutinaRepository.delete(rutina);
    }

    private RutinaDTO mapToRutinaDTO(Rutina rutina) {
        return new RutinaDTO(
                rutina.getId(),
                rutina.getAtletaId(),
                rutina.getNombre(),
                rutina.getDescripcion(),
                rutina.getCreadoEn(),
                rutina.getSesiones().stream().map(sesion -> new grit.sistema.backend.dto.training.SesionRutinaDTO(
                        sesion.getId(),
                        sesion.getNombre(),
                        sesion.getOrden(),
                        sesion.getEjercicios().stream().map(ejercicio -> new EjercicioResponseDTO(
                                ejercicio.getId(),
                                ejercicio.getEjercicioId(),
                                ejercicio.getEjercicioNombre(),
                                ejercicio.getSeries(),
                                ejercicio.getReps(),
                                ejercicio.getNotas(),
                                ejercicio.getOrden()
                        )).toList()
                )).toList()
        );
    }
}
