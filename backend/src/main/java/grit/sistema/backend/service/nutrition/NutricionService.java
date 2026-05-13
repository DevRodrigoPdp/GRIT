package grit.sistema.backend.service.nutrition;

import grit.sistema.backend.dto.nutrition.*;
import java.util.List;
import java.util.UUID;

public interface NutricionService {
    // Gestión de Planes
    PlanNutricionActivoResponseDTO getPlanNutricionActivoAtleta(UUID atletaId);
    void activarPlan(UUID entrenadorId, UUID planId);
    void desactivarPlan(UUID entrenadorId, UUID planId);
    List<PlanNutricionResponseDTO> listarPlanes(UUID entrenadorId, UUID atletaId);
    PlanNutricionResponseDTO crearPlan(UUID entrenadorId, PlanNutricionRequestDTO request);
    PlanNutricionResponseDTO actualizarPlan(UUID entrenadorId, UUID planId, PlanNutricionRequestDTO request);
    void eliminarPlan(UUID entrenadorId, UUID planId);
}
