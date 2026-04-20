package grit.sistema.backend.repository;

import grit.sistema.backend.model.nutrition.Receta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RecetaRepository extends JpaRepository<Receta, UUID> {
    List<Receta> findAllByEntrenadorId(UUID entrenadorId);
}
