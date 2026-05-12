package grit.sistema.backend.dto.nutrition;


import java.util.List;

public record ComidaResponseDTO(
        String nombre,
        String notas,
        List<AlimentoDTO> alimentos
) {
}
