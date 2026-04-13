package grit.sistema.backend.dto;

public record FileResponseDTO(
        String fileName,
        String uploadStatus,
        long size
) {}
