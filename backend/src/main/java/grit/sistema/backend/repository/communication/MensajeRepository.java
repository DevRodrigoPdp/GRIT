package grit.sistema.backend.repository.communication;

import grit.sistema.backend.entity.communication.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, UUID> {
    // Recupera mensajes ordenados cronológicamente
    List<Mensaje> findByHiloIdOrderByEnviadoEnAsc(UUID hiloId);
}
