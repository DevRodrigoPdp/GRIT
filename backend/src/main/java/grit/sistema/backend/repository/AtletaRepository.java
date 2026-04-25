package grit.sistema.backend.repository;

import grit.sistema.backend.model.coaching.Atleta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AtletaRepository extends JpaRepository<Atleta, UUID> {

    Optional<Atleta> findByEmail(String email);

    boolean existsByEmail(String email);
}
