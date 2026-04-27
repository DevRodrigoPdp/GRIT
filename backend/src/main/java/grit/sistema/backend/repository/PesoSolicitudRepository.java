package grit.sistema.backend.repository;

import grit.sistema.backend.entity.training.enums.EstadoCheckin;
import grit.sistema.backend.entity.training.PesoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PesoSolicitudRepository extends JpaRepository<PesoSolicitud, UUID> {
    Optional<PesoSolicitud> findByAtletaIdAndEstado(UUID atletaId, EstadoCheckin estado);
    boolean existsByAtletaIdAndEstado(UUID atletaId, EstadoCheckin estado);
}
