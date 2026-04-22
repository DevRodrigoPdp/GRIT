package api.alimentos.service;

import api.alimentos.dto.AlimentoCrearRequestDTO;
import api.alimentos.dto.AlimentoResponseDTO;
import api.alimentos.model.Alimento;
import api.alimentos.repository.AlimentoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
     * @param q Texto a buscar (mínimo 2 caracteres)
     * @return Lista de alimentos encontrados (máximo 15)
     */
    @Transactional(readOnly = true)
    public List<AlimentoResponseDTO> buscarAlimentos(String q) {
        log.info("Buscando alimentos con término: '{}'", q);

        // Si q es nulo, vacío o tiene menos de 2 caracteres, devolver lista vacía
        if (q == null || q.trim().isEmpty() || q.trim().length() < 2) {
            log.info("Búsqueda rechazada: término insuficiente");
            return List.of();
        }

        List<Alimento> alimentos = alimentoRepository.buscarPorNombreOMarca(q.trim());
        log.info("Se encontraron {} alimentos para el término '{}'", alimentos.size(), q);

        return alimentos.stream()
                .map(this::mapToResponseDTO)
                .toList();
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
     * Mapea una entidad Alimento a AlimentoResponseDTO.
     */
    private AlimentoResponseDTO mapToResponseDTO(Alimento alimento) {
        return AlimentoResponseDTO.builder()
                .id(alimento.getId())
                .nombre(alimento.getNombre())
                .marca(alimento.getMarca())
                .kcalPor100g(alimento.getKcalPor100g())
                .proteinasPor100g(alimento.getProteinasPor100g())
                .carbsPor100g(alimento.getCarbsPor100g())
                .grasasPor100g(alimento.getGrasasPor100g())
                .build();
    }
}
