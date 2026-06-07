package grit.sistema.backend.service.nutrition;

import grit.sistema.backend.dto.nutrition.AlimentoCrearRequestDTO;
import grit.sistema.backend.dto.nutrition.AlimentoResponseDTO;
import grit.sistema.backend.entity.nutrition.Alimento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface AlimentoService {

    Page<AlimentoResponseDTO> buscarAlimentos(String query, String categoria, Pageable pageable);

    Page<AlimentoResponseDTO> buscadorGlobal(String query, Pageable pageable);

    List<String> listarCategorias();

    AlimentoResponseDTO crearAlimento(AlimentoCrearRequestDTO request, UUID usuarioId);

    Alimento obtenerAlimentoPorId(UUID id);
}