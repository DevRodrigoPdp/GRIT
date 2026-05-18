package grit.sistema.backend.service.communication.impl;

import grit.sistema.backend.dto.communication.*;
import grit.sistema.backend.entity.Usuario;
import grit.sistema.backend.entity.coaching.Asignacion;
import grit.sistema.backend.entity.coaching.Atleta;
import grit.sistema.backend.entity.coaching.enums.TipoServicio;
import grit.sistema.backend.entity.communication.Hilo;
import grit.sistema.backend.entity.communication.LecturaHilo;
import grit.sistema.backend.entity.communication.LecturaHiloId;
import grit.sistema.backend.entity.communication.Mensaje;
import grit.sistema.backend.entity.communication.enums.ContextoHilo;
import grit.sistema.backend.exception.security.AccesoDenegadoException;
import grit.sistema.backend.mapper.communication.HiloMapper;
import grit.sistema.backend.mapper.communication.MensajeMapper;
import grit.sistema.backend.repository.coaching.AsignacionRepository;
import grit.sistema.backend.repository.coaching.AtletaRepository;
import grit.sistema.backend.repository.communication.HiloRepository;
import grit.sistema.backend.repository.communication.LecturaHiloRepository;
import grit.sistema.backend.repository.user.UsuarioRepository;
import grit.sistema.backend.service.common.StorageService;
import grit.sistema.backend.service.communication.HiloService;
import grit.sistema.backend.service.communication.MensajeService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class HiloServiceImpl implements HiloService {
    private static final List<String> TIPOS_IMAGEN = List.of("image/jpeg", "image/png", "image/webp");
    private static final List<String> TIPOS_VIDEO = List.of("video/mp4", "video/mpeg", "video/quicktime");
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB

    private final MensajeService mensajeService;
    private final HiloMapper hiloMapper;
    private final MensajeMapper mensajeMapper;
    private final HiloRepository hiloRepository;
    private final LecturaHiloRepository lecturaRepository;
    private final UsuarioRepository usuarioRepository;
    private final StorageService storageService;
    private final AtletaRepository atletaRepository;
    private final AsignacionRepository asignacionRepository;

    @Override
    @Transactional
    public HiloDetalleDTO crearHilo(CrearHiloDTO dto, List<MultipartFile> archivos, UUID emisorId) {
        log.info("Iniciando creación de hilo: '{}'", dto.titulo());

        Atleta atleta = atletaRepository.findById(dto.atletaId())
                .orElseThrow(() -> new EntityNotFoundException("Atleta no encontrado"));

        Asignacion asignacion = asignacionRepository.findByAtletaIdAndActivaTrueAndTipoServicio(atleta.getId(), TipoServicio.valueOf(dto.contexto().name()))
                .orElseThrow(() -> new IllegalStateException("El atleta no tiene un entrenamiento activo"));

        Usuario emisor = usuarioRepository.findById(emisorId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario emisor no encontrado"));

        if (emisor.getRol().name().equals("ATLETA") && !atleta.getId().equals(emisorId)) {
            throw new AccesoDenegadoException("No puedes crear hilos para otro atleta");
        }

        List<AdjuntoData> adjuntosSubidos = procesarSubidaS3(archivos);

        Hilo guardado = mensajeService.crearHiloConPrimerMensaje(dto, adjuntosSubidos, atleta, asignacion.getEntrenador(), emisor);

        return hiloMapper.toDetalleDTO(guardado, true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HiloResumenDTO> obtenerHilosPorAtleta(UUID atletaId, ContextoHilo contexto, UUID solicitanteId) {
        if (!atletaId.equals(solicitanteId)) {
            boolean esSuEntrenador = asignacionRepository.existsByAtletaIdAndEntrenadorIdAndActivaTrue(atletaId, solicitanteId);
            if (!esSuEntrenador) {
                throw new AccessDeniedException("No tienes permiso para ver los hilos de este atleta.");
            }
        }

        return hiloRepository.findResumenByAtletaAndContexto(atletaId, contexto, solicitanteId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HiloResumenDTO> obtenerHilosParaEntrenador(UUID atletaId, UUID entrenadorId, ContextoHilo contexto) {
        return hiloRepository.findResumenByAtletaForEntrenador(atletaId, entrenadorId, contexto);
    }

    @Override
    @Transactional
    public HiloDetalleDTO obtenerDetalleHilo(UUID hiloId, UUID usuarioId) {
        Hilo hilo = hiloRepository.findById(hiloId)
                .orElseThrow(() -> new EntityNotFoundException("Hilo no encontrado"));

        validarAccesoAHilo(hilo, usuarioId);

        boolean yaLeido = lecturaRepository.existsByHiloIdAndUsuarioId(hiloId, usuarioId);

        HiloDetalleDTO dto = hiloMapper.toDetalleDTO(hilo, yaLeido);

        actualizarEstadoLectura(hilo, usuarioRepository.getReferenceById(usuarioId));

        return dto;
    }

    @Override
    public MensajeDTO responderHilo(UUID hiloId, String texto, List<MultipartFile> archivos, UUID emisorId) {
        log.info("Iniciando respuesta al hilo {}", hiloId);
        Hilo hilo = hiloRepository.findById(hiloId)
                .orElseThrow(() -> new EntityNotFoundException("Hilo no encontrado"));

        validarAccesoAHilo(hilo, emisorId);

        List<AdjuntoData> adjuntosSubidos = procesarSubidaS3(archivos);

        Mensaje guardado = mensajeService.salvarMensaje(hiloId, texto, adjuntosSubidos, emisorId);

        return mensajeMapper.toDTO(guardado);
    }

    @Override
    @Transactional
    public void marcarComoLeido(UUID hiloId, UUID usuarioId) {
        log.debug("Marcando hilo {} como leído para usuario {}", hiloId, usuarioId);

        Hilo hilo = hiloRepository.findById(hiloId)
                .orElseThrow(() -> new EntityNotFoundException("Hilo no encontrado"));

        validarAccesoAHilo(hilo, usuarioId);

        Usuario usuario = usuarioRepository.getReferenceById(usuarioId);
        actualizarEstadoLectura(hilo, usuario);
    }

    private void actualizarEstadoLectura(Hilo hilo, Usuario usuario) {
        LecturaHilo lectura = lecturaRepository.findById(new LecturaHiloId(hilo.getId(), usuario.getId()))
                .orElse(new LecturaHilo(hilo, usuario));
        lectura.setLeidoEn(LocalDateTime.now());
        lecturaRepository.save(lectura);
    }

    private void validarAccesoAHilo(Hilo hilo, UUID usuarioId) {
        boolean esAtleta = hilo.getAtleta().getId().equals(usuarioId);
        boolean esEntrenador = hilo.getEntrenador().getId().equals(usuarioId);
        if (!esAtleta && !esEntrenador) {
            throw new AccesoDenegadoException("No tienes permiso para ver este hilo");
        }
    }

    private List<AdjuntoData> procesarSubidaS3(List<MultipartFile> archivos) {
        if (archivos == null || archivos.isEmpty()) return List.of();

        return archivos.stream()
                .map(file -> {
                    validarArchivo(file); // Tu método de validación
                    String key = storageService.uploadFile(file); // Llamada a S3
                    return new AdjuntoData(key, file.getOriginalFilename(), determinarTipo(file.getContentType()));
                })
                .toList();
    }

    private void validarArchivo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío o es nulo");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    String.format("El archivo %s excede el límite de 100MB", file.getOriginalFilename())
            );
        }

        String contentType = file.getContentType();
        if (contentType == null || (!TIPOS_IMAGEN.contains(contentType) && !TIPOS_VIDEO.contains(contentType))) {
            throw new UnsupportedOperationException("Formato de archivo no permitido: " + contentType);
        }
    }

    /**
     * Clasifica el archivo en las categorías de tu lógica de negocio (IMAGEN o VIDEO).
     */
    private String determinarTipo(String contentType) {
        if (contentType == null) return "OTRO";

        if (contentType.startsWith("image/")) {
            return "IMAGEN";
        } else if (contentType.startsWith("video/")) {
            return "VIDEO";
        }

        return "DESCONOCIDO";
    }
}