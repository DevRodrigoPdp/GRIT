package grit.sistema.backend.repository;

import grit.sistema.backend.entity.training.Rutina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RutinaRepository extends JpaRepository<Rutina, UUID> {
    List<Rutina> findAllByEntrenadorId(UUID entrenadorId);
    List<Rutina> findAllByEntrenadorIdAndAtletaId(UUID entrenadorId, UUID atletaId);
    Optional<Rutina> findByIdAndEntrenadorId(UUID id, UUID entrenadorId);
    Optional<Rutina> findFirstByAtletaIdOrderByCreadoEnDesc(UUID atletaId);
}
