package grit.sistema.backend.service.coaching;

import grit.sistema.backend.dto.coaching.AtletaResumenDTO;
import grit.sistema.backend.dto.coaching.EntrenadorPerfilDTO;
import grit.sistema.backend.dto.coaching.EntrenadorRequestDTO;
import grit.sistema.backend.dto.coaching.EntrenadorResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface EntrenadorService {

    EntrenadorResponseDTO registrarEntrenador(EntrenadorRequestDTO request,
                                              MultipartFile fotoPerfil,
                                              List<MultipartFile> certificaciones);

    EntrenadorPerfilDTO obtenerPerfil(String email);

    List<AtletaResumenDTO> listarMisAtletas(String email);

    void solicitarBajaCuenta(UUID entrenadorId);
}