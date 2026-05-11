package grit.sistema.backend.service.training.impl;

import grit.sistema.backend.dto.training.EjercicioRequestDTO;
import grit.sistema.backend.dto.training.RutinaDTO;
import grit.sistema.backend.dto.training.RutinaRequestDTO;
import grit.sistema.backend.dto.training.RutinaResponseDTO;
import grit.sistema.backend.entity.training.Ejercicio;
import grit.sistema.backend.entity.training.EjercicioEnSesion;
import grit.sistema.backend.entity.training.Rutina;
import grit.sistema.backend.entity.training.SesionRutina;
import grit.sistema.backend.exception.security.AccesoDenegadoException;
import grit.sistema.backend.mapper.training.EntrenamientoMapper;
import grit.sistema.backend.repository.coaching.AtletaRepository;
import grit.sistema.backend.repository.coaching.EntrenadorRepository;
import grit.sistema.backend.repository.training.EjercicioRepository;
import grit.sistema.backend.repository.training.RutinaRepository;
import grit.sistema.backend.service.training.EntrenamientoService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EntrenamientoServiceImpl implements EntrenamientoService {
    private final RutinaRepository rutinaRepository;
    private final AtletaRepository atletaRepository;
    private final EntrenadorRepository entrenadorRepository;
    private final EjercicioRepository ejercicioRepository;
    private final EntrenamientoMapper mapper;

    @Override
    @Transactional
    public RutinaResponseDTO crearRutina(UUID entrenadorId, RutinaRequestDTO request) {
        // 1. Validaciones
        var atleta = atletaRepository.findById(request.atletaId()).orElseThrow();
        var entrenador = entrenadorRepository.findById(entrenadorId).orElseThrow();

        // 2. Carga masiva del catálogo (Optimización)
        Set<UUID> idsEjercicios = request.sesiones().stream()
                .flatMap(s -> s.ejercicios().stream())
                .map(EjercicioRequestDTO::ejercicioId)
                .collect(Collectors.toSet());
        Map<UUID, Ejercicio> catalogo = ejercicioRepository.findAllById(idsEjercicios).stream()
                .collect(Collectors.toMap(Ejercicio::getId, e -> e));

        log.info("IDs enviados desde el front: {}", idsEjercicios);
        log.info("IDs encontrados en la base de datos: {}", catalogo.keySet());

        if (catalogo.size() != idsEjercicios.size()) {
            List<UUID> faltantes = idsEjercicios.stream()
                    .filter(id -> !catalogo.containsKey(id))
                    .toList();
            throw new EntityNotFoundException("Error de integridad: Los siguientes IDs de ejercicios no existen en el catálogo: " + faltantes);
        }

        // 3. Construcción Jerárquica usando el Mapper
        Rutina rutina = new Rutina();
        rutina.setNombre(request.nombre());
        rutina.setDescripcion(request.descripcion());
        rutina.setAtleta(atleta);
        rutina.setEntrenador(entrenador);
        rutina.setActivo(request.activo());

        for (var sDto : request.sesiones()) {
            SesionRutina sesion = new SesionRutina();
            sesion.setNombre(sDto.nombre());
            sesion.setOrden(sDto.orden().shortValue());

            for (var eDto : sDto.ejercicios()) {
                Ejercicio maestro = catalogo.get(eDto.ejercicioId());
                if (maestro == null) throw new EntityNotFoundException("Ejercicio no existe");

                // LLAMADA AL MAPPER: Mezcla el DTO con el Maestro
                EjercicioEnSesion detalle = mapper.toEjercicioEnSesion(eDto, maestro);
                sesion.addEjercicio(detalle);
            }
            rutina.addSesion(sesion);
        }

        return mapper.toResponseDTO(rutinaRepository.save(rutina));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RutinaResponseDTO> listarRutinas(UUID entrenadorId, UUID atletaId) {

        List<Rutina> rutinas = (atletaId == null)
                ? rutinaRepository.findAllByEntrenadorIdOrderByCreadoEnDesc(entrenadorId)
                : rutinaRepository.findAllByEntrenadorIdAndAtletaIdOrderByCreadoEnDesc(entrenadorId, atletaId);

        return rutinas.stream()
                .map(mapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public void eliminarRutina(UUID entrenadorId, UUID rutinaId) {
        // 1. Buscar la entidad o lanzar 404
        Rutina rutina = rutinaRepository.findById(rutinaId)
                .orElseThrow(() -> new EntityNotFoundException("La rutina no existe"));

        // 2. Validación de propiedad (Seguridad a nivel de datos)
        if (!rutina.getEntrenador().getId().equals(entrenadorId)) {
            log.warn("Intento de borrado no autorizado: Entrenador {} sobre rutina {}", entrenadorId, rutinaId);
            throw new AccessDeniedException("No tienes permisos para eliminar esta rutina");
        }

        // 3. Borrado en cascada (gestionado por CascadeType.ALL en la entidad)
        rutinaRepository.delete(rutina);
        log.info("Rutina {} eliminada correctamente", rutinaId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RutinaDTO> getPlanEntrenamientoActivoAtleta(UUID atletaId) {
        return rutinaRepository.findByAtletaIdAndActivoTrue(atletaId)
                .map(mapper::toDTO);
    }

    @Override
    @Transactional
    public void activarRutina(UUID entrenadorId, UUID rutinaId) {
        Rutina rutina = rutinaRepository.findById(rutinaId)
                .orElseThrow(() -> new EntityNotFoundException("Rutina con ID " + rutinaId + " no encontrada"));

        validarPropiedad(entrenadorId, rutina);

        rutinaRepository.desactivarRutinasActivas(rutina.getAtleta().getId());

        rutina.setActivo(true);
        rutinaRepository.saveAndFlush(rutina);

        log.info("Rutina {} activada para el atleta {}", rutinaId, rutina.getAtleta().getId());
    }

    private void validarPropiedad(UUID entrenadorId, Rutina rutina) {
        if (!rutina.getEntrenador().getId().equals(entrenadorId)) {
            throw new AccesoDenegadoException("No tienes permiso sobre esta rutina");
        }
    }
}
