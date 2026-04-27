package grit.sistema.backend.repository.training;

import grit.sistema.backend.entity.training.PesoCheckin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PesoCheckinRepository extends JpaRepository<PesoCheckin, UUID> {
    // Para la gráfica: ordenado por fecha ASC
    List<PesoCheckin> findAllByAtletaIdOrderByFechaAsc(UUID atletaId);
}
