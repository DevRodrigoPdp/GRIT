package grit.sistema.backend.service.nutrition.impl;

import grit.sistema.backend.dto.nutrition.PlanNutricionActivoResponseDTO;
import grit.sistema.backend.dto.nutrition.PlanNutricionRequestDTO;
import grit.sistema.backend.dto.nutrition.PlanNutricionResponseDTO;
import grit.sistema.backend.entity.nutrition.PlanNutricion;
import grit.sistema.backend.entity.training.Rutina;
import grit.sistema.backend.exception.security.AccesoDenegadoException;
import grit.sistema.backend.mapper.nutrition.NutricionMapper;
import grit.sistema.backend.repository.coaching.AtletaRepository;
import grit.sistema.backend.repository.coaching.EntrenadorRepository;
import grit.sistema.backend.repository.nutrition.PlanNutricionRepository;
import grit.sistema.backend.service.nutrition.NutricionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NutricionServiceImpl implements NutricionService {
    private final PlanNutricionRepository planRepository;
    private final AtletaRepository atletaRepository;
    private final EntrenadorRepository entrenadorRepository;
    private final NutricionMapper mapper;
    private final CacheManager cacheManager;

    private static final String CACHE_PLAN_ACTIVO = "planNutricionActivo";

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_PLAN_ACTIVO, key = "#atletaId")
    public PlanNutricionActivoResponseDTO getPlanNutricionActivoAtleta(UUID atletaId) {
        return planRepository.findByAtletaIdAndActivoTrue(atletaId)
                .map(plan -> new PlanNutricionActivoResponseDTO(true, mapper.toDataDTO(plan)))
                .orElse(new PlanNutricionActivoResponseDTO(true, null));
    }

    @Override
    @Transactional
    public void activarPlan(UUID entrenadorId, UUID planId) {
        PlanNutricion plan = planRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("Plan no encontrado"));

        validarPropiedad(entrenadorId, plan);
        UUID atletaId = plan.getAtleta().getId();

        evictPlanActivo(atletaId);

        planRepository.desactivarPlanesActivos(atletaId);
        plan.setActivo(true);

        planRepository.saveAndFlush(plan);

        log.info("Plan de nutrición {} activado para atleta {}", planId, plan.getAtleta().getId());
    }

    @Override
    @Transactional
    public void desactivarPlan(UUID entrenadorId, UUID planId) {
        PlanNutricion plan = planRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("Plan no encontrado"));

        if (!plan.getEntrenador().getId().equals(entrenadorId)) {
            throw new AccessDeniedException("No tienes permiso para modificar este plan");
        }

        if (!plan.isActivo()) {
            log.info("El plan {} ya se encuentra desactivado.", planId);
            return;
        }

        plan.setActivo(false);
        planRepository.save(plan);

        evictPlanActivo(plan.getAtleta().getId());

        log.info("Plan {} desactivado por el entrenador {}", planId, entrenadorId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanNutricionResponseDTO> listarPlanes(UUID entrenadorId, UUID atletaId) {
        List<PlanNutricion> planes = (atletaId == null)
                ? planRepository.findAllByEntrenadorIdOrderByCreadoEnDesc(entrenadorId)
                : planRepository.findAllByEntrenadorIdAndAtletaIdOrderByCreadoEnDesc(entrenadorId, atletaId);

        return planes.stream()
                .map(mapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public PlanNutricionResponseDTO crearPlan(UUID entrenadorId, PlanNutricionRequestDTO request) {
        var atleta = atletaRepository.findById(request.atletaId())
                .orElseThrow(() -> new EntityNotFoundException("Atleta no encontrado"));
        var entrenador = entrenadorRepository.findById(entrenadorId)
                .orElseThrow(() -> new EntityNotFoundException("Entrenador no encontrado"));

        PlanNutricion plan = mapper.toEntity(request);
        plan.setEntrenador(entrenador);
        plan.setAtleta(atleta);

        plan.setKcalDiarias(request.kcalDiarias());
        plan.setProteinas(request.proteinas());
        plan.setCarbos(request.carbos());
        plan.setGrasas(request.grasas());

        if (request.activo()) {
            planRepository.findByAtletaIdAndActivoTrue(atleta.getId())
                    .ifPresent(p -> p.setActivo(false));
        }
        plan.setActivo(request.activo());

        evictPlanActivo(plan.getAtleta().getId());

        return mapper.toResponseDTO(planRepository.save(plan));
    }

    @Override
    @Transactional
    public PlanNutricionResponseDTO actualizarPlan(UUID entrenadorId, UUID planId, PlanNutricionRequestDTO request) {
        PlanNutricion planExistente = planRepository.findByIdAndEntrenadorId(planId, entrenadorId)
                .orElseThrow(() -> new EntityNotFoundException("Plan no encontrado"));

        UUID atletaId = planExistente.getAtleta().getId();

        planExistente.setNombre(request.nombre());
        planExistente.setDescripcion(request.descripcion());
        planExistente.setKcalDiarias(request.kcalDiarias());
        planExistente.setProteinas(request.proteinas());
        planExistente.setCarbos(request.carbos());
        planExistente.setGrasas(request.grasas());

        if (request.activo() && !planExistente.isActivo()) {
            planRepository.findByAtletaIdAndActivoTrue(planExistente.getAtleta().getId())
                    .ifPresent(p -> p.setActivo(false));
        }
        planExistente.setActivo(request.activo());

        PlanNutricion datosNuevos = mapper.toEntity(request);

        planExistente.setComidas(datosNuevos.getComidas());

        evictPlanActivo(atletaId);

        return mapper.toResponseDTO(planRepository.save(planExistente));
    }

    @Override
    @Transactional
    public void eliminarPlan(UUID entrenadorId, UUID planId) {
        PlanNutricion plan = planRepository.findByIdAndEntrenadorId(planId, entrenadorId)
                .orElseThrow(() -> new EntityNotFoundException("Plan no encontrado"));

        validarPropiedad(entrenadorId, plan);

        UUID atletaId = plan.getAtleta().getId();

        planRepository.delete(plan);

        evictPlanActivo(atletaId);
    }

    private void validarPropiedad(UUID entrenadorId, PlanNutricion plan) {
        if (!plan.getEntrenador().getId().equals(entrenadorId)) {
            throw new AccesoDenegadoException("No tienes permiso sobre esta plan");
        }
    }

    private void evictPlanActivo(UUID atletaId) {
        Optional.ofNullable(cacheManager.getCache(CACHE_PLAN_ACTIVO))
                .ifPresent(c -> c.evict(atletaId));
    }
}
