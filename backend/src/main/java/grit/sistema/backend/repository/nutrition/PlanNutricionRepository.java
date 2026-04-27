package grit.sistema.backend.repository.nutrition;

import grit.sistema.backend.entity.nutrition.PlanNutricion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlanNutricionRepository extends JpaRepository<PlanNutricion, UUID> {
    List<PlanNutricion> findAllByEntrenadorId(UUID entrenadorId);
    List<PlanNutricion> findAllByEntrenadorIdAndAtletaId(UUID entrenadorId, UUID atletaId);
    Optional<PlanNutricion> findByIdAndEntrenadorId(UUID id, UUID entrenadorId);
}
