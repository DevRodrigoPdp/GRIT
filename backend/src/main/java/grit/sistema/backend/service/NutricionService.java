package grit.sistema.backend.service;

import grit.sistema.backend.dto.nutrition.*;
import grit.sistema.backend.mapper.NutricionMapper;
import grit.sistema.backend.model.nutrition.AlimentoReciente;
import grit.sistema.backend.model.nutrition.PlanNutricion;
import grit.sistema.backend.repository.AlimentoRecienteRepository;
import grit.sistema.backend.repository.AtletaRepository;
import grit.sistema.backend.repository.EntrenadorRepository;
import grit.sistema.backend.repository.PlanNutricionRepository;
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
    private final NutricionMapper mapper; // Inyectamos el Mapper

    @Transactional(readOnly = true)
    public List<PlanNutricionDTO> listarPlanes(UUID entrenadorId, UUID atletaId) {
        List<PlanNutricion> planes = (atletaId == null)
                ? planRepository.findAllByEntrenadorId(entrenadorId)
                : planRepository.findAllByEntrenadorIdAndAtletaId(entrenadorId, atletaId);

        return planes.stream().map(mapper::toDTO).toList();
    }

    @Transactional
    public PlanNutricionResponseDTO crearPlan(UUID entrenadorId, PlanNutricionRequestDTO request) {
        // 1. Validaciones de existencia (Regla de negocio)
        var atleta = atletaRepository.findById(request.atletaId())
                .orElseThrow(() -> new EntityNotFoundException("Atleta no encontrado"));
        var entrenador = entrenadorRepository.findById(entrenadorId)
                .orElseThrow(() -> new EntityNotFoundException("Entrenador no encontrado"));

        // 2. Uso de MapStruct para convertir el Request en Entidad
        PlanNutricion plan = mapper.toEntity(request);

        // 3. Vincular relaciones core (JPA maneja el resto por Cascada)
        plan.setEntrenador(entrenador);
        plan.setAtleta(atleta);

        // Importante: Asegurar bidireccionalidad si MapStruct no lo hace automáticamente
        plan.getComidas().forEach(comida -> {
            comida.setPlan(plan);
            comida.getAlimentos().forEach(alimento -> alimento.setComida(comida));
        });

        PlanNutricion guardado = planRepository.save(plan);
        return mapper.toResponseDTO(guardado);
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
        // 1. Obtener entidades de la DB
        List<AlimentoReciente> entidades = recienteRepository
                .findTop8ByUsuarioIdAndNombreComidaOrderByUsadoEnDesc(usuarioId, nombreComida);

        // 2. Mapear explícitamente al DTO de salida
        return entidades.stream()
                .map(mapper::toAlimentoRecienteDTO)
                .toList();
    }

    @Transactional
    public void eliminarPlan(UUID entrenadorId, UUID planId) {
        PlanNutricion plan = planRepository.findByIdAndEntrenadorId(planId, entrenadorId)
                .orElseThrow(() -> new EntityNotFoundException("Plan no encontrado o no pertenece al entrenador"));

        planRepository.delete(plan);
    }
}
