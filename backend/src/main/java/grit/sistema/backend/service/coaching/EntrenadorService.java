package grit.sistema.backend.service.coaching;

import grit.sistema.backend.dto.coaching.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface EntrenadorService {

    EntrenadorResponseDTO registrarEntrenador(EntrenadorRequestDTO request,
                                              MultipartFile fotoPerfil,
                                              List<MultipartFile> certificaciones);

    EntrenadorPerfilDTO obtenerPerfil(UUID entrenadorId);

    EntrenadorPerfilDTO editarPerfil(UUID entrenadorId, EntrenadorEditarPerfilDTO request);

    List<AtletaResumenDTO> listarMisAtletas(String email);

    EntrenadorResponseDTO ampliarFormacion(UUID entrenadorId,
                                           AmpliarFormacionDTO dto,
                                           List<MultipartFile> documentos);

    void solicitarBajaCuenta(UUID entrenadorId);
}