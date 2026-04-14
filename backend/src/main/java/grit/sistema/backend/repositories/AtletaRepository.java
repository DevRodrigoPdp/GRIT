package grit.sistema.backend.repositories;

import grit.sistema.backend.model.coaching.Atleta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AtletaRepository extends JpaRepository<Atleta, Long> {

    Optional<Atleta> findByEmail(String email);

    Optional<Atleta> findById(UUID id);
}
