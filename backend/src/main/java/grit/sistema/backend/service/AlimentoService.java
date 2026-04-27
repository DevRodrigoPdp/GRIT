package grit.sistema.backend.service;

import grit.sistema.backend.dto.AlimentoCrearRequestDTO;
import grit.sistema.backend.dto.AlimentoResponseDTO;
import grit.sistema.backend.entity.nutrition.Alimento;
import grit.sistema.backend.repository.AlimentoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Servicio de gestión de alimentos.
 * Responsable de las operaciones CRUD y búsquedas de alimentos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlimentoService {

    private final AlimentoRepository alimentoRepository;

    /**
     * Busca alimentos por nombre o marca.
     * Si q tiene menos de 2 caracteres o es nulo/vacío, devuelve una lista vacía.
     * Devuelve máximo 15 resultados ordenados por relevancia.
     *
     * @param query Texto a buscar (mínimo 2 caracteres)
     * @return Lista de alimentos encontrados (máximo 15)
     */
    @Transactional(readOnly = true)
    public Page<AlimentoResponseDTO> buscarAlimentos(String query, String categoria, Pageable pageable) {
        Page<Alimento> alimentos;

        String q = (query != null) ? query.trim() : "";
        String cat = (categoria != null) ? categoria.trim() : "";

        if (!q.isEmpty() && !cat.isEmpty()) {
            // Filtro avanzado: texto + categoría
            alimentos = alimentoRepository.buscarPorNombreYCategoria(q, cat, pageable);
        } else if (!q.isEmpty()) {
            // Buscador global (nombre, marca o categoría coincidente con q)
            alimentos = alimentoRepository.buscadorGlobal(q, pageable);
        } else if (!cat.isEmpty()) {
            // Solo filtro por categoría
            alimentos = alimentoRepository.findByCategoriaContainingIgnoreCase(cat, pageable);
        } else {
            // Sin filtros: listado general
            alimentos = alimentoRepository.findAll(pageable);
        }

        return alimentos.map(this::mapToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<AlimentoResponseDTO> buscadorGlobal(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return alimentoRepository.findAll(pageable).map(this::mapToResponseDTO);
        }
        // Llama a la Query Nativa con el CASE WHEN de relevancia
        return alimentoRepository.buscadorGlobal(query.trim(), pageable).map(this::mapToResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<String> listarCategorias() {
        return alimentoRepository.obtenerCategoriasUnicas();
    }

    /**
     * Crea un nuevo alimento personalizado.
     * Solo disponible para entrenadores con titulación en nutrición.
     *
     * @param request Datos del alimento a crear
     * @param usuarioId ID del entrenador/nutricionista que crea el alimento
     * @return El alimento creado
     */
    @Transactional
    public AlimentoResponseDTO crearAlimento(AlimentoCrearRequestDTO request, UUID usuarioId) {
        log.info("Creando nuevo alimento: {} (marca: {})", request.getNombre(), request.getMarca());

        Alimento alimento = Alimento.builder()
                .nombre(request.getNombre().trim())
                .marca(request.getMarca() != null ? request.getMarca().trim() : null)
                .kcalPor100g(request.getKcalPor100g())
                .proteinasPor100g(request.getProteinasPor100g())
                .carbsPor100g(request.getCarbsPor100g())
                .grasasPor100g(request.getGrasasPor100g())
                .creadoPor(usuarioId)
                .build();

        Alimento alimentoGuardado = alimentoRepository.save(alimento);
        log.info("Alimento creado exitosamente con ID: {}", alimentoGuardado.getId());

        return mapToResponseDTO(alimentoGuardado);
    }

    /**
     * Obtiene un alimento por su ID.
     *
     * @param id ID del alimento
     * @return El alimento encontrado
     * @throws EntityNotFoundException si no existe el alimento
     */
    @Transactional(readOnly = true)
    public Alimento obtenerAlimentoPorId(UUID id) {
        log.info("Obteniendo alimento con ID: {}", id);
        return alimentoRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Alimento no encontrado con ID: {}", id);
                    return new EntityNotFoundException("Alimento no encontrado");
                });
    }

    /**
     * Mapea una entidad Alimento a AlimentoResponseDTO (Record).
     */
    private AlimentoResponseDTO mapToResponseDTO(Alimento alimento) {
        return new AlimentoResponseDTO(
                alimento.getId(),
                alimento.getNombre(),
                alimento.getMarca(),
                alimento.getCategoria(),
                alimento.getKcalPor100g(),
                alimento.getProteinasPor100g(),
                alimento.getCarbsPor100g(),
                alimento.getGrasasPor100g()
        );
    }
}
