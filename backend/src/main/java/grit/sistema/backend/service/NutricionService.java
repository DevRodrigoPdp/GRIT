package grit.sistema.backend.service;

import grit.sistema.backend.dto.nutrition.AlimentoRecienteDTO;
import grit.sistema.backend.dto.nutrition.AlimentoRecienteRequestDTO;
import grit.sistema.backend.dto.nutrition.PlanNutricionDTO;
import grit.sistema.backend.dto.nutrition.PlanNutricionRequestDTO;
import grit.sistema.backend.dto.nutrition.PlanNutricionResponseDTO;
import grit.sistema.backend.dto.nutrition.RecetaRequestDTO;
import grit.sistema.backend.dto.nutrition.RecetaResponseDTO;
import grit.sistema.backend.model.nutrition.AlimentoEnComida;
import grit.sistema.backend.model.nutrition.AlimentoReciente;
import grit.sistema.backend.model.nutrition.Comida;
import grit.sistema.backend.model.nutrition.PlanNutricion;
import grit.sistema.backend.model.nutrition.Receta;
import grit.sistema.backend.model.nutrition.IngredienteReceta;
import grit.sistema.backend.repository.AlimentoRecienteRepository;
import grit.sistema.backend.repository.AtletaRepository;
import grit.sistema.backend.repository.PlanNutricionRepository;
import grit.sistema.backend.repository.RecetaRepository;
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
public class NutricionService {
    private final PlanNutricionRepository planRepository;
    private final RecetaRepository recetaRepository;
    private final AlimentoRecienteRepository recienteRepository;
    private final AtletaRepository atletaRepository;

    @Transactional(readOnly = true)
    public List<PlanNutricionDTO> listarPlanes(UUID entrenadorId, UUID atletaId) {
        List<PlanNutricion> planes = atletaId == null
                ? planRepository.findAllByEntrenadorId(entrenadorId)
                : planRepository.findAllByEntrenadorIdAndAtletaId(entrenadorId, atletaId);

        return planes.stream().map(this::mapToDTO).toList();
    }

    @Transactional
    public PlanNutricionResponseDTO crearPlan(UUID entrenadorId, PlanNutricionRequestDTO request) {
        atletaRepository.findById(request.atletaId())
                .orElseThrow(() -> new EntityNotFoundException("Atleta no encontrado"));

        PlanNutricion plan = new PlanNutricion();
        plan.setEntrenadorId(entrenadorId);
        plan.setAtletaId(request.atletaId());
        plan.setNombre(request.nombre());
        plan.setDescripcion(request.descripcion());
        plan.setCreadoEn(OffsetDateTime.now());

        List<Comida> comidas = request.comidas().stream().map(comidaRequest -> {
            Comida comida = new Comida();
            comida.setPlan(plan);
            comida.setNombre(comidaRequest.nombre());
            comida.setOrden((short) (request.comidas().indexOf(comidaRequest) + 1));
            List<AlimentoEnComida> alimentos = comidaRequest.alimentos().stream().map(alimentoRequest -> {
                AlimentoEnComida alimento = new AlimentoEnComida();
                alimento.setComida(comida);
                alimento.setCodigoAlimento(alimentoRequest.codigo());
                alimento.setNombre(alimentoRequest.nombre());
                alimento.setMarca(alimentoRequest.marca());
                alimento.setKcalPor100g(alimentoRequest.kcalPor100g());
                alimento.setProteinasPor100g(alimentoRequest.proteinasPor100g());
                alimento.setCarbsPor100g(alimentoRequest.carbsPor100g());
                alimento.setGrasasPor100g(alimentoRequest.grasasPor100g());
                alimento.setCantidadG(alimentoRequest.cantidadG());
                return alimento;
            }).toList();
            comida.setAlimentos(alimentos);
            return comida;
        }).toList();

        plan.setComida(comidas);
        PlanNutricion guardado = planRepository.save(plan);
        return new PlanNutricionResponseDTO(guardado.getId(), guardado.getCreadoEn());
    }

    @Transactional
    public void eliminarPlan(UUID entrenadorId, UUID planId) {
        PlanNutricion plan = planRepository.findByIdAndEntrenadorId(planId, entrenadorId)
                .orElseThrow(() -> new EntityNotFoundException("Plan de nutrición no encontrado"));

        if (!plan.getEntrenadorId().equals(entrenadorId)) {
            throw new AccessDeniedException("No tienes permiso para eliminar este plan");
        }

        planRepository.delete(plan);
    }

    @Transactional(readOnly = true)
    public List<RecetaResponseDTO> listarRecetas(UUID entrenadorId) {
        return recetaRepository.findAllByEntrenadorId(entrenadorId)
                .stream()
                .map(this::mapToRecetaResponse)
                .toList();
    }

    @Transactional
    public RecetaResponseDTO crearReceta(UUID entrenadorId, RecetaRequestDTO request) {
        Receta receta = new Receta();
        receta.setEntrenadorId(entrenadorId);
        receta.setNombre(request.nombre());
        receta.setGramosTotal(request.gramosTotal());
        receta.setCreadoEn(OffsetDateTime.now());

        List<IngredienteReceta> ingredientes = request.ingredientes().stream().map(ingredienteRequest -> {
            IngredienteReceta ingrediente = new IngredienteReceta();
            ingrediente.setReceta(receta);
            ingrediente.setCodigoAlimento(ingredienteRequest.codigo());
            ingrediente.setNombre(ingredienteRequest.nombre());
            ingrediente.setKcalPor100g(ingredienteRequest.kcalPor100g());
            ingrediente.setCantidadG(ingredienteRequest.cantidadG());
            ingrediente.setOrden((short) (request.ingredientes().indexOf(ingredienteRequest) + 1));
            return ingrediente;
        }).toList();

        receta.setIngredientes(ingredientes);
        Receta guardada = recetaRepository.save(receta);
        return mapToRecetaResponse(guardada);
    }

    @Transactional
    public void eliminarReceta(UUID entrenadorId, UUID recetaId) {
        Receta receta = recetaRepository.findById(recetaId)
                .orElseThrow(() -> new EntityNotFoundException("Receta no encontrada"));

        if (!receta.getEntrenadorId().equals(entrenadorId)) {
            throw new AccessDeniedException("No tienes permiso para eliminar esta receta");
        }

        recetaRepository.delete(receta);
    }

    @Transactional(readOnly = true)
    public List<AlimentoRecienteDTO> listarAlimentosRecientes(UUID entrenadorId, String nombreComida) {
        return recienteRepository.findTop8ByUsuarioIdAndNombreComidaOrderByUsadoEnDesc(entrenadorId, nombreComida)
                .stream()
                .map(this::mapToAlimentoRecienteDTO)
                .toList();
    }

    @Transactional
    public void registrarAlimentoReciente(UUID entrenadorId, AlimentoRecienteRequestDTO request) {
        AlimentoReciente existente = recienteRepository
                .findByUsuarioIdAndNombreComidaAndCodigoAlimento(entrenadorId, request.nombreComida(), request.alimento().codigo())
                .orElse(null);

        if (existente != null) {
            existente.setUsadoEn(OffsetDateTime.now());
            existente.setNombre(request.alimento().nombre());
            existente.setMarca(request.alimento().marca());
            existente.setKcalPor100g(request.alimento().kcalPor100g());
            existente.setProteinasPor100g(request.alimento().proteinasPor100g());
            existente.setCarbsPor100g(request.alimento().carbsPor100g());
            existente.setGrasasPor100g(request.alimento().grasasPor100g());
            recienteRepository.save(existente);
            return;
        }

        AlimentoReciente reciente = new AlimentoReciente();
        reciente.setUsuarioId(entrenadorId);
        reciente.setNombreComida(request.nombreComida());
        reciente.setCodigoAlimento(request.alimento().codigo());
        reciente.setNombre(request.alimento().nombre());
        reciente.setMarca(request.alimento().marca());
        reciente.setKcalPor100g(request.alimento().kcalPor100g());
        reciente.setProteinasPor100g(request.alimento().proteinasPor100g());
        reciente.setCarbsPor100g(request.alimento().carbsPor100g());
        reciente.setGrasasPor100g(request.alimento().grasasPor100g());
        reciente.setUsadoEn(OffsetDateTime.now());
        recienteRepository.save(reciente);

        long count = recienteRepository.countByUsuarioIdAndNombreComida(entrenadorId, request.nombreComida());
        if (count > 8) {
            List<AlimentoReciente> todos = recienteRepository.findTop8ByUsuarioIdAndNombreComidaOrderByUsadoEnDesc(entrenadorId, request.nombreComida());
            if (todos.size() == 8) {
                AlimentoReciente masAntiguo = todos.get(todos.size() - 1);
                recienteRepository.delete(masAntiguo);
            }
        }
    }

    private PlanNutricionDTO mapToDTO(PlanNutricion plan) {
        return new PlanNutricionDTO(
                plan.getId(),
                plan.getAtletaId(),
                plan.getNombre(),
                plan.getDescripcion(),
                plan.getCreadoEn(),
                plan.getComida().stream().map(comida -> new grit.sistema.backend.dto.nutrition.ComidaDTO(
                        comida.getId(),
                        comida.getNombre(),
                        comida.getOrden(),
                        comida.getAlimentos().stream().map(alimento -> new grit.sistema.backend.dto.nutrition.AlimentoDTO(
                                alimento.getCodigoAlimento(),
                                alimento.getNombre(),
                                alimento.getMarca(),
                                alimento.getKcalPor100g(),
                                alimento.getProteinasPor100g(),
                                alimento.getCarbsPor100g(),
                                alimento.getGrasasPor100g(),
                                alimento.getCantidadG()
                        )).toList()
                )).toList()
        );
    }

    private RecetaResponseDTO mapToRecetaResponse(Receta receta) {
        return new RecetaResponseDTO(
                receta.getId(),
                receta.getNombre(),
                receta.getGramosTotal(),
                receta.getCreadoEn(),
                receta.getIngredientes().stream().map(ingrediente -> new grit.sistema.backend.dto.nutrition.IngredienteRecetaResponseDTO(
                        ingrediente.getCodigoAlimento(),
                        ingrediente.getNombre(),
                        ingrediente.getKcalPor100g(),
                        ingrediente.getCantidadG(),
                        ingrediente.getOrden()
                )).toList()
        );
    }

    private AlimentoRecienteDTO mapToAlimentoRecienteDTO(AlimentoReciente reciente) {
        return new AlimentoRecienteDTO(
                reciente.getCodigoAlimento(),
                reciente.getNombre(),
                reciente.getMarca(),
                reciente.getKcalPor100g(),
                reciente.getProteinasPor100g(),
                reciente.getCarbsPor100g(),
                reciente.getGrasasPor100g()
        );
    }
}
