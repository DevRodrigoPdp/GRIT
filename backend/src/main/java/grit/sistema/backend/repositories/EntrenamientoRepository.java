package com.sistema.gritfitprueba.repositories;

import com.sistema.gritfitprueba.model.Entrenamiento;
import com.sistema.gritfitprueba.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EntrenamientoRepository extends JpaRepository<Entrenamiento, Integer> {
    List<Entrenamiento> findAllByUsuarioEmail(String email);

    Optional<Entrenamiento> findByUuid(UUID uuid);
}
