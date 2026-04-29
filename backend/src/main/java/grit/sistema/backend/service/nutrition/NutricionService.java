package grit.sistema.backend.service.nutrition;

import grit.sistema.backend.dto.nutrition.*;
import grit.sistema.backend.mapper.nutrition.NutricionMapper;
import grit.sistema.backend.entity.nutrition.AlimentoReciente;
import grit.sistema.backend.entity.nutrition.PlanNutricion;
import grit.sistema.backend.repository.nutrition.AlimentoRecienteRepository;
import grit.sistema.backend.repository.coaching.AtletaRepository;
import grit.sistema.backend.repository.coaching.EntrenadorRepository;
import grit.sistema.backend.repository.nutrition.PlanNutricionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NutricionService {
    private final PlanNutricionRepository planRepository;
    private final AlimentoRecienteRepository recienteRepository;
    private final AtletaRepository atletaRepository;
    private final EntrenadorRepository entrenadorRepository;
    private final NutricionMapper mapper;

    @Transactional(readOnly = true)
    public List<PlanNutricionResponseDTO> listarPlanes(UUID entrenadorId, UUID atletaId) {
        List<PlanNutricion> planes = (atletaId == null)
                ? planRepository.findAllByEntrenadorId(entrenadorId)
                : planRepository.findAllByEntrenadorIdAndAtletaId(entrenadorId, atletaId);
        return planes.stream().map(mapper::toResponseDTO).toList();
    }

    @Transactional
    public PlanNutricionResponseDTO crearPlan(UUID entrenadorId, PlanNutricionRequestDTO request) {
        var atleta = atletaRepository.findById(request.atletaId())
                .orElseThrow(() -> new EntityNotFoundException("Atleta no encontrado"));
        var entrenador = entrenadorRepository.findById(entrenadorId)
                .orElseThrow(() -> new EntityNotFoundException("Entrenador no encontrado"));

        PlanNutricion plan = mapper.toEntity(request);
        plan.setEntrenador(entrenador);
        plan.setAtleta(atleta);

        // La bidireccionalidad la maneja el @AfterMapping del mapper
        return mapper.toResponseDTO(planRepository.save(plan));
    }

    @Transactional
    public PlanNutricionResponseDTO actualizarPlan(UUID entrenadorId, UUID planId, PlanNutricionRequestDTO request) {
        // 1. Validar propiedad y existencia
        PlanNutricion planExistente = planRepository.findByIdAndEntrenadorId(planId, entrenadorId)
                .orElseThrow(() -> new EntityNotFoundException("Plan no encontrado o acceso denegado"));

        // 2. Actualizar datos básicos (el atleta no se cambia por regla de negocio)
        planExistente.setNombre(request.nombre());
        planExistente.setDescripcion(request.descripcion());

        // 3. Limpiar jerarquía antigua (orphanRemoval = true se encarga del resto)
        planExistente.getComidas().clear();
        planRepository.saveAndFlush(planExistente); // Limpia la DB antes de insertar lo nuevo

        // 4. Mapear y añadir nuevas comidas (sin IDs para evitar 'detached entity')
        PlanNutricion datosNuevos = mapper.toEntity(request);
        if (datosNuevos.getComidas() != null) {
            datosNuevos.getComidas().forEach(comida -> {
                comida.setId(null); // Triple seguro contra detached
                comida.setPlan(planExistente);
                comida.getAlimentos().forEach(a -> a.setId(null));
                planExistente.getComidas().add(comida);
            });
        }

        return mapper.toResponseDTO(planRepository.save(planExistente));
    }

    @Transactional
    public void eliminarPlan(UUID entrenadorId, UUID planId) {
        PlanNutricion plan = planRepository.findByIdAndEntrenadorId(planId, entrenadorId)
                .orElseThrow(() -> new EntityNotFoundException("Plan no encontrado"));
        planRepository.delete(plan);
    }

    @Transactional
    public void registrarAlimentoReciente(UUID usuarioId, AlimentoRecienteRequestDTO request) {
        AlimentoReciente reciente = recienteRepository
                .findByUsuarioIdAndNombreComidaAndAlimentoId(usuarioId, request.nombreComida(), request.alimento().id())
                .orElseGet(() -> {
                    AlimentoReciente nuevo = mapper.toAlimentoRecienteEntity(request.alimento());
                    nuevo.setUsuarioId(usuarioId);
                    nuevo.setNombreComida(request.nombreComida());
                    return nuevo;
                });
        reciente.setUsadoEn(OffsetDateTime.now());
        recienteRepository.save(reciente);
    }

    @Transactional(readOnly = true)
    public List<AlimentoRecienteDTO> listarAlimentosRecientes(UUID usuarioId, String nombreComida) {
        return recienteRepository
                .findTop8ByUsuarioIdAndNombreComidaOrderByUsadoEnDesc(usuarioId, nombreComida)
                .stream()
                .map(mapper::toAlimentoRecienteDTO).toList();
    }
}
