package grit.sistema.backend.dto.nutrition;


import java.util.List;

public record ComidaResponseDTO(
        String nombre,
        String descripcion,
        List<AlimentoDTO> alimentos
) {
}
