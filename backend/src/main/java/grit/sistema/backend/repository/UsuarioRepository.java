package grit.sistema.backend.repository;

import grit.sistema.backend.model.Usuario;
import grit.sistema.backend.model.enums.EstadoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findById(UUID id);

    List<Usuario> findByEstado(EstadoUsuario estado);

    boolean existsByEmail(String email);
}
