package grit.sistema.backend.service.communication;

import grit.sistema.backend.dto.communication.CrearHiloDTO;
import grit.sistema.backend.dto.communication.HiloResumenDTO;
import grit.sistema.backend.entity.Usuario;
import grit.sistema.backend.entity.coaching.Asignacion;
import grit.sistema.backend.entity.coaching.Atleta;
import grit.sistema.backend.entity.coaching.Entrenador;
import grit.sistema.backend.entity.communication.*;
import grit.sistema.backend.entity.communication.enums.ContextoHilo;
import grit.sistema.backend.exception.security.AccesoDenegadoException;
import grit.sistema.backend.repository.coaching.AsignacionRepository;
import grit.sistema.backend.repository.coaching.AtletaRepository;
import grit.sistema.backend.repository.communication.HiloRepository;
import grit.sistema.backend.repository.communication.LecturaHiloRepository;
import grit.sistema.backend.repository.communication.MensajeRepository;
import grit.sistema.backend.repository.user.UsuarioRepository;
import grit.sistema.backend.service.common.StorageService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final HiloRepository hiloRepository;
    private final MensajeRepository mensajeRepository;
    private final LecturaHiloRepository lecturaRepository;
    private final UsuarioRepository usuarioRepository;
    private final StorageService storageService; // Interfaz para subir a S3
    private final AtletaRepository atletaRepository;
    private final AsignacionRepository asignacionRepository;

    @Override
    @Transactional
    public Hilo crearHilo(CrearHiloDTO dto, List<MultipartFile> archivos, UUID emisorId) {
        log.info("Creando hilo: '{}' para Atleta ID: {}", dto.titulo(), dto.atletaId());
// 1. Validar que el atleta existe
        Atleta atleta = atletaRepository.findById(dto.atletaId())
                .orElseThrow(() -> new EntityNotFoundException("Atleta no encontrado"));

        // 2. BUSCAR EL ENTRENADOR A TRAVÉS DE LA ASIGNACIÓN ACTIVA
        Asignacion asignacion = asignacionRepository.findByAtletaIdAndActivaTrue(atleta.getId())
                .orElseThrow(() -> new IllegalStateException("El atleta no tiene un entrenamiento activo"));

        Entrenador entrenador = asignacion.getEntrenador();

        // 3. Validar permisos (Si el emisor es el atleta, debe ser SU ID)
        Usuario emisor = usuarioRepository.findById(emisorId).orElseThrow();
        if (emisor.getRol().name().equals("ATLETA") && !atleta.getId().equals(emisorId)) {
            throw new AccesoDenegadoException("No puedes crear hilos para otro atleta");
        }

        // 4. Crear el hilo con el entrenador de la asignación
        Hilo hilo = new Hilo();
        hilo.setId(UUID.randomUUID());
        hilo.setTitulo(dto.titulo());
        hilo.setCategoria(dto.categoria());
        hilo.setContexto(dto.contexto());
        hilo.setAtleta(atleta);
        hilo.setEntrenador(entrenador);
        hilo.setCreadoPor(emisor.getRol().name());

        // 4. Crear Mensaje inicial
        Mensaje mensaje = new Mensaje();
        mensaje.setTexto(dto.texto());
        mensaje.setEnviadoPor(emisor.getRol().name());
        mensaje.setHilo(hilo);
        mensaje.setEnviadoEn(LocalDateTime.now());

        // 5. Procesar adjuntos usando el método privado robusto
        procesarAdjuntos(archivos, mensaje);

        hilo.getMensajes().add(mensaje);

        // Al guardar Hilo, se guardará el mensaje y los adjuntos por CascadeType.ALL
        Hilo guardado = hiloRepository.save(hilo);

        // 6. Actualizar registro de lectura
        actualizarEstadoLectura(guardado, emisor);

        return guardado;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HiloResumenDTO> obtenerHilosPorAtleta(UUID atletaId, ContextoHilo contexto, UUID usuarioId) {
        // Usamos la query optimizada del repositorio
        return hiloRepository.findResumenByAtletaAndContexto(atletaId, contexto, usuarioId);
    }

    @Override
    @Transactional
    public Hilo obtenerDetalleHilo(UUID hiloId, UUID usuarioId) {
        Hilo hilo = hiloRepository.findById(hiloId)
                .orElseThrow(() -> new EntityNotFoundException("Hilo no encontrado"));

        // SEGURIDAD SENIOR: Validar que el usuario pertenece al hilo
        validarAccesoAHilo(hilo, usuarioId);

        // Al abrirlo, marcamos como leído para este usuario
        Usuario usuario = usuarioRepository.getReferenceById(usuarioId);
        actualizarEstadoLectura(hilo, usuario);

        return hilo;
    }

    @Override
    @Transactional
    public Mensaje responderHilo(UUID hiloId, String texto, List<MultipartFile> archivos, UUID emisorId) {
        log.info("Usuario {} respondiendo al hilo {}", emisorId, hiloId);

        // 1. Recuperar el hilo y validar existencia
        Hilo hilo = hiloRepository.findById(hiloId)
                .orElseThrow(() -> new EntityNotFoundException("Hilo no encontrado"));

        // 2. Validar que el emisor pertenece al hilo
        validarAccesoAHilo(hilo, emisorId);

        Usuario emisor = usuarioRepository.getReferenceById(emisorId);

        // 3. Crear y configurar el nuevo mensaje
        Mensaje mensaje = new Mensaje();
        mensaje.setTexto(texto);
        mensaje.setEnviadoPor(emisor.getRol().name());
        mensaje.setHilo(hilo);
        mensaje.setEnviadoEn(LocalDateTime.now());

        // 4. Procesar adjuntos (reutilizando la lógica que ya tenemos)
        if (archivos != null && !archivos.isEmpty()) {
            procesarAdjuntos(archivos, mensaje);
        }

        // 5. Persistir mensaje y actualizar fecha del hilo para ordenamiento
        Mensaje guardado = mensajeRepository.save(mensaje);

        // 6. Sincronizar lectura: El emisor está al día
        actualizarEstadoLectura(hilo, emisor);

        return guardado;
    }

    @Override
    @Transactional
    public void marcarComoLeido(UUID hiloId, UUID usuarioId) {
        log.debug("Marcando hilo {} como leído para usuario {}", hiloId, usuarioId);

        Hilo hilo = hiloRepository.findById(hiloId)
                .orElseThrow(() -> new EntityNotFoundException("Hilo no encontrado"));

        // Validamos que el usuario pueda marcarlo como leído
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

    private void procesarAdjuntos(List<MultipartFile> archivos, Mensaje mensaje) {
        if (archivos == null || archivos.isEmpty()) {
            return;
        }

        for (MultipartFile file : archivos) {
            // 1. Validación de seguridad básica (Max 100MB por archivo)
            if (file.getSize() > 100 * 1024 * 1024) {
                throw new IllegalArgumentException("El archivo " + file.getOriginalFilename() + " excede el límite de 100MB");
            }

            // 2. Subida física al almacenamiento (S3)
            // El storageService debe devolver la 'key' única (ej. un UUID)
            String s3Key = storageService.uploadFile(file);

            // 3. Creación del objeto de metadatos
            Adjunto adjunto = new Adjunto();
            adjunto.setS3Key(s3Key);
            adjunto.setNombreOriginal(file.getOriginalFilename());
            adjunto.setMensaje(mensaje);

            // 4. Determinación del tipo de medio
            String contentType = file.getContentType();
            if (contentType != null && contentType.startsWith("video")) {
                adjunto.setTipo("VIDEO");
            } else if (contentType != null && contentType.startsWith("image")) {
                adjunto.setTipo("IMAGEN");
            } else {
                throw new UnsupportedOperationException("Tipo de archivo no permitido: " + contentType);
            }

            // 5. Vincular al mensaje (Relación bidireccional)
            mensaje.getAdjuntos().add(adjunto);
        }
    }
}