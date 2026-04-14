package grit.sistema.backend.service;

import grit.sistema.backend.dto.ApiResponseDTO;
import grit.sistema.backend.dto.entrenador.DocumentoDTO;
import grit.sistema.backend.dto.entrenador.EntrenadorPendienteDTO;
import grit.sistema.backend.dto.entrenador.EntrenadorResponseDTO;
import grit.sistema.backend.dto.entrenador.RechazoDTO;
import grit.sistema.backend.model.Usuario;
import grit.sistema.backend.model.coaching.Entrenador;
import grit.sistema.backend.model.enums.EstadoRevision;
import grit.sistema.backend.model.enums.EstadoUsuario;
import grit.sistema.backend.repository.EntrenadorRepository;
import grit.sistema.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {
    private final EntrenadorRepository entrenadorRepository;
    private final StorageService storageService;

    public List<EntrenadorPendienteDTO> listarEntrenadoresPorEstado(String estado) {
        EstadoRevision estadoEnum = EstadoRevision.valueOf(estado.toUpperCase());

//        List<EntrenadorResponseDTO> data = entrenadorRepository.findByEstado(estadoEnum)
//                .stream()
//                .map(u -> new EntrenadorResponseDTO(
//                        u.getId(),
//                        u.getUsuario(),
//                        u.getCorreo(),
//                        u.getTitulacionEntrenamiento(),
//                        u.getTitulacionNutricion(),
//                        u.getCodigoProfesional(),
//                        u.getCreatedAt(),
//                        u.getDocumentos().stream().map(doc -> new DocumentoDTO(
//                                doc.getId(),
//                                doc.getNombreArchivo(),
//                                s3Service.generatePresignedUrl(doc.getS3Key()), // URL firmada de 15 min
//                                doc.getUploadedAt()
//                        )).toList()
//                )).toList();
//
//        return new ApiResponseDTO<>(true, data);
        return List.of();
    }

    @Transactional
    public void rechazarEntrenador(UUID id, String motivo) {
        // 1. Buscar con Optional para evitar NullPointerException
        Entrenador entrenador = entrenadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entrenador no encontrado"));

        // 2. Cambiar estado
        entrenador.setEstado(EstadoRevision.RECHAZADO);
         // Atributo en la Entity

        // 3. Persistir
//        usuarioRepository.save(entrenador);

    }
}
