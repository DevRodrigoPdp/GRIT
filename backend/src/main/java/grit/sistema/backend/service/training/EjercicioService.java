package grit.sistema.backend.service.training;

import grit.sistema.backend.dto.training.EjercicioDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EjercicioService {

    /**
     * Busca ejercicios por nombre y/o grupo muscular de forma paginada.
     */
    Page<EjercicioDTO> buscarEjercicios(String nombre, String grupoMuscular, Pageable pageable);

    /**
     * Buscador global que busca tanto en nombre como en grupo muscular con un solo término.
     */
    Page<EjercicioDTO> buscadorGlobal(String query, Pageable pageable);
}
