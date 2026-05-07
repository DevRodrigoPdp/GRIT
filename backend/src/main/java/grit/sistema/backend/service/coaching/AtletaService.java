package grit.sistema.backend.service.coaching;

import grit.sistema.backend.dto.coaching.ProfesionalAsignadoDTO;
import grit.sistema.backend.dto.coaching.AtletaPerfilDTO;
import grit.sistema.backend.dto.coaching.AtletaRequestDTO;
import grit.sistema.backend.dto.coaching.AtletaResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface AtletaService {

    AtletaResponseDTO registrarAtleta(AtletaRequestDTO dto, MultipartFile foto);

    AtletaPerfilDTO obtenerPerfil(UUID atletaId);

    List<ProfesionalAsignadoDTO> getProfesionalesAsignados(UUID atletaId);

    void solicitarBajaCuenta(UUID atletaId);
}
