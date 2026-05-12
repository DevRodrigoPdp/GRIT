package grit.sistema.backend.dto.coaching;

public record AtletaEditarPerfilDTO(
        String deporte,
        Integer altura,
        String nivel,
        String objetivo,
        Integer peso
) {
}
