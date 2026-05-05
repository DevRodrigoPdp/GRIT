package grit.sistema.backend.service.nutrition.impl;

import grit.sistema.backend.dto.nutrition.*;
import grit.sistema.backend.entity.nutrition.AlimentoReciente;
import grit.sistema.backend.entity.nutrition.NotaNutricionista;
import grit.sistema.backend.entity.nutrition.PlanNutricion;
import grit.sistema.backend.mapper.nutrition.NutricionMapper;
import grit.sistema.backend.repository.coaching.AtletaRepository;
import grit.sistema.backend.repository.coaching.EntrenadorRepository;
import grit.sistema.backend.repository.nutrition.AlimentoRecienteRepository;
import grit.sistema.backend.repository.nutrition.NotaNutricionistaRepository;
import grit.sistema.backend.repository.nutrition.PlanNutricionRepository;
import grit.sistema.backend.service.nutrition.NutricionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NutricionServiceImpl implements NutricionService {
    private final PlanNutricionRepository planRepository;
    private final AlimentoRecienteRepository recienteRepository;
    private final AtletaRepository atletaRepository;
    private final EntrenadorRepository entrenadorRepository;
    private final NotaNutricionistaRepository notaNutricionistaRepository;
    private final NutricionMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public PlanNutricionActivoResponseDTO getPlanNutricionActivoAtleta(UUID atletaId) {
        return planRepository.findByAtletaIdAndActivoTrue(atletaId)
                .map(plan -> new PlanNutricionActivoResponseDTO(true, mapper.toDataDTO(plan)))
                .orElse(new PlanNutricionActivoResponseDTO(true, null));
    }

    @Override
    @Transactional
    public void activarPlan(UUID entrenadorId, UUID planId) {
        // 1. Obtener el plan validando que pertenezca al entrenador (Seguridad)
        PlanNutricion plan = planRepository.findByIdAndEntrenadorId(planId, entrenadorId)
                .orElseThrow(() -> new EntityNotFoundException("Plan nutricional no encontrado"));

        // 2. Desactivación atómica directa en DB
        // Esto garantiza que el índice UNIQUE no salte al activar el siguiente
        planRepository.desactivarPlanesActivos(plan.getAtleta().getId());

        // 3. Activar el nuevo y sincronizar
        plan.setActivo(true);
        planRepository.saveAndFlush(plan); // saveAndFlush es clave aquí

        log.info("Plan de nutrición {} activado para atleta {}", planId, plan.getAtleta().getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanNutricionResponseDTO> listarPlanes(UUID entrenadorId, UUID atletaId) {
        List<PlanNutricion> planes = (atletaId == null)
                ? planRepository.findAllByEntrenadorId(entrenadorId)
                : planRepository.findAllByEntrenadorIdAndAtletaId(entrenadorId, atletaId);
        return planes.stream().map(mapper::toResponseDTO).toList();
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

        // Sincronizar macros globales del request (BigDecimal/Double según decidiste)
        plan.setKcalDiarias(request.kcalDiarias());
        plan.setProteinas(request.proteinas());
        plan.setCarbos(request.carbos());
        plan.setGrasas(request.grasas());

        // Si el request dice que este plan nace activo, ejecutamos la lógica de desactivación previa
        if (request.activo()) {
            planRepository.findByAtletaIdAndActivoTrue(atleta.getId())
                    .ifPresent(p -> p.setActivo(false));
        }
        plan.setActivo(request.activo());

        return mapper.toResponseDTO(planRepository.save(plan));
    }

    @Override
    @Transactional
    public PlanNutricionResponseDTO actualizarPlan(UUID entrenadorId, UUID planId, PlanNutricionRequestDTO request) {
        PlanNutricion planExistente = planRepository.findByIdAndEntrenadorId(planId, entrenadorId)
                .orElseThrow(() -> new EntityNotFoundException("Plan no encontrado"));

        // 1. Actualización de campos básicos (MapStruct podría hacerlo, pero así es muy seguro)
        planExistente.setNombre(request.nombre());
        planExistente.setDescripcion(request.descripcion());
        planExistente.setKcalDiarias(request.kcalDiarias());
        planExistente.setProteinas(request.proteinas());
        planExistente.setCarbos(request.carbos());
        planExistente.setGrasas(request.grasas());

        // 2. Manejo de estado activo
        if (request.activo() && !planExistente.isActivo()) {
            planRepository.findByAtletaIdAndActivoTrue(planExistente.getAtleta().getId())
                    .ifPresent(p -> p.setActivo(false));
        }
        planExistente.setActivo(request.activo());

        // 3. LA CLAVE: Usamos el método de conveniencia
        // Primero obtenemos las entidades del mapper
        PlanNutricion datosNuevos = mapper.toEntity(request);

        // Esto dispara el orphanRemoval de forma controlada por Hibernate
        planExistente.setComidas(datosNuevos.getComidas());

        return mapper.toResponseDTO(planRepository.save(planExistente));
    }

    @Override
    @Transactional
    public void eliminarPlan(UUID entrenadorId, UUID planId) {
        PlanNutricion plan = planRepository.findByIdAndEntrenadorId(planId, entrenadorId)
                .orElseThrow(() -> new EntityNotFoundException("Plan no encontrado"));
        planRepository.delete(plan);
    }

    @Override
    @Transactional
    public void registrarAlimentoReciente(UUID usuarioId, AlimentoRecienteRequestDTO request) {
        AlimentoReciente reciente = recienteRepository
                .findByUsuarioIdAndNombreComidaAndAlimentoId(
                        usuarioId,
                        request.nombreComida(),
                        request.alimento().id()
                )
                .map(existente -> {
                    existente.setUsadoEn(OffsetDateTime.now());
                    return existente;
                })
                .orElseGet(() -> {
                    AlimentoReciente nuevo = mapper.toAlimentoRecienteEntity(request.alimento());
                    nuevo.setUsuarioId(usuarioId);
                    nuevo.setNombreComida(request.nombreComida());
                    return nuevo;
                });

        recienteRepository.save(reciente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlimentoRecienteDTO> listarAlimentosRecientes(UUID usuarioId, String nombreComida) {
        return recienteRepository
                .findTop8ByUsuarioIdAndNombreComidaOrderByUsadoEnDesc(usuarioId, nombreComida)
                .stream()
                .map(mapper::toAlimentoRecienteDTO).toList();
    }

    @Override
    @Transactional
    public NotaResponseDTO crearNota(UUID entrenadorId, UUID atletaId, NotaNutricionistaRequestDTO request) {
        // 1. Validaciones de existencia (Criterio Profesional)
        var entrenador = entrenadorRepository.findById(entrenadorId)
                .orElseThrow(() -> new RuntimeException("Entrenador no encontrado"));

        var atleta = atletaRepository.findById(atletaId)
                .orElseThrow(() -> new RuntimeException("Atleta no encontrado"));

        // 2. Mapeo manual de DTO a Entity (Evita exponer la entidad al exterior)
        NotaNutricionista nuevaNota = new NotaNutricionista();
        nuevaNota.setTexto(request.texto());
        nuevaNota.setEntrenador(entrenador);
        nuevaNota.setAtleta(atleta);

        // 3. Persistencia
        NotaNutricionista guardada = notaNutricionistaRepository.save(nuevaNota);
        return new NotaResponseDTO(guardada.getId(), guardada.getTexto(), guardada.getFecha());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotaResponseDTO> obtenerNotasAtleta(UUID atletaId) {
        return notaNutricionistaRepository.findByAtletaIdOrderByCreadaEnDesc(atletaId)
                .stream()
                .map(n -> new NotaResponseDTO(n.getId(), n.getTexto(), n.getFecha()))
                .toList();
    }
}
