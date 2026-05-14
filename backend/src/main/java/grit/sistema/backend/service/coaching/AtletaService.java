package grit.sistema.backend.service.coaching;

import grit.sistema.backend.dto.coaching.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface AtletaService {

    AtletaResponseDTO registrarAtleta(AtletaRequestDTO dto, MultipartFile foto);

    AtletaPerfilDTO obtenerPerfil(UUID atletaId);

    AtletaPerfilDTO editarPerfil(UUID atletaId, AtletaEditarPerfilDTO dto);

    List<ProfesionalAsignadoDTO> getProfesionalesAsignados(UUID atletaId);

    void solicitarBajaCuenta(String email);
}
