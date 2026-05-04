package grit.sistema.backend.service.training.impl;

import grit.sistema.backend.dto.training.EjercicioDTO;
import grit.sistema.backend.entity.training.Ejercicio;
import grit.sistema.backend.repository.training.EjercicioRepository;
import grit.sistema.backend.service.training.EjercicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EjercicioServiceImpl implements EjercicioService {
    private final EjercicioRepository repository;

    /**
     * Busca ejercicios por nombre y/o grupo muscular de forma paginada.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<EjercicioDTO> buscarEjercicios(String nombre, String grupoMuscular, Pageable pageable) {
        Page<Ejercicio> ejercicios;

        String nombreTrim = (nombre != null) ? nombre.trim() : null;
        String grupoTrim = (grupoMuscular != null) ? grupoMuscular.trim() : null;

        if (estaVacio(nombreTrim) && !estaVacio(grupoTrim)) {
            // El usuario solo eligió un grupo muscular (usamos la búsqueda parcial mejorada)
            ejercicios = repository.findByGrupoMuscularContainingIgnoreCase(grupoTrim, pageable);

        } else if (!estaVacio(nombreTrim) && estaVacio(grupoTrim)) {
            // El usuario solo escribió en el buscador de texto
            ejercicios = repository.findByNombreContainingIgnoreCase(nombreTrim, pageable);

        } else if (!estaVacio(nombreTrim) && !estaVacio(grupoTrim)) {
            // El usuario filtró por ambos campos
            // USAMOS LA NUEVA QUERY FLEXIBLE O LA COMBINADA
            ejercicios = repository.findByNombreContainingIgnoreCaseAndGrupoMuscularContainingIgnoreCase(nombreTrim, grupoTrim, pageable);

        } else {
            // Sin filtros: devolver todo paginado
            ejercicios = repository.findAll(pageable);
        }

        return ejercicios.map(this::convertirADTO);
    }

    /**
     * Buscador global que busca tanto en nombre como en grupo muscular con un solo término.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<EjercicioDTO> buscadorGlobal(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return repository.findAll(pageable).map(this::convertirADTO);
        }

        String cleanQuery = query.trim()
                .replace("%", "\\%")
                .replace("_", "\\_");

        // Ignoramos el sort del controlador para aplicar nuestro ranking
        Pageable queryPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());

        return repository.buscarFlexible(cleanQuery, queryPageable)
                .map(this::convertirADTO);
    }

    // Helper para limpiar el código de nulos y vacíos
    private boolean estaVacio(String str) {
        return str == null || str.isBlank();
    }

    private EjercicioDTO convertirADTO(Ejercicio e) {
        return new EjercicioDTO(
                e.getId(),
                e.getNombre(),
                e.getGrupoMuscular(),
                e.getDificultad() != null ? e.getDificultad().toString() : "N/A",
                e.getEquipoNecesario()
        );
    }
}
