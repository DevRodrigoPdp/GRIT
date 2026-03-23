package com.sistema.gritfitprueba.repositories;

import com.sistema.gritfitprueba.dto.UsuarioDTO;
import com.sistema.gritfitprueba.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByUuid(UUID uuid);

    boolean existsByEmail(String email);
}
