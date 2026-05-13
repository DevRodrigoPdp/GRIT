package grit.sistema.backend.service.communication.impl;

import grit.sistema.backend.dto.communication.AdjuntoData;
import grit.sistema.backend.dto.communication.CrearHiloDTO;
import grit.sistema.backend.entity.Usuario;
import grit.sistema.backend.entity.coaching.Atleta;
import grit.sistema.backend.entity.coaching.Entrenador;
import grit.sistema.backend.entity.communication.*;
import grit.sistema.backend.repository.communication.HiloRepository;
import grit.sistema.backend.repository.communication.LecturaHiloRepository;
import grit.sistema.backend.repository.communication.MensajeRepository;
import grit.sistema.backend.repository.user.UsuarioRepository;
import grit.sistema.backend.service.communication.MensajeService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MensajeServiceImpl implements MensajeService {
    private final MensajeRepository mensajeRepository;
    private final HiloRepository hiloRepository;
    private final UsuarioRepository usuarioRepository;
    private final LecturaHiloRepository lecturaRepository;

    @Override
    @Transactional
    public Mensaje salvarMensaje(UUID hiloId, String texto, List<AdjuntoData> adjuntosData, UUID emisorId) {
        Hilo hilo = hiloRepository.findById(hiloId)
                .orElseThrow(() -> new EntityNotFoundException("Hilo no encontrado"));

        Usuario emisor = usuarioRepository.getReferenceById(emisorId);

        Mensaje mensaje = new Mensaje();
        mensaje.setTexto(texto);
        mensaje.setEnviadoPor(emisor.getRol().name());
        mensaje.setHilo(hilo);
        mensaje.setEnviadoEn(LocalDateTime.now());

        adjuntosData.forEach(data -> {
            Adjunto entidad = new Adjunto();
            entidad.setS3Key(data.s3Key());
            entidad.setNombreOriginal(data.nombreOriginal());
            entidad.setTipo(data.tipo());
            entidad.setMensaje(mensaje);
            mensaje.getAdjuntos().add(entidad);
        });

        actualizarEstadoLectura(hilo, emisor);
        return mensajeRepository.save(mensaje);
    }

    private void actualizarEstadoLectura(Hilo hilo, Usuario usuario) {
        LecturaHilo lectura = lecturaRepository.findById(new LecturaHiloId(hilo.getId(), usuario.getId()))
                .orElse(new LecturaHilo(hilo, usuario));
        lectura.setLeidoEn(LocalDateTime.now());
        lecturaRepository.save(lectura);
    }

    @Override
    @Transactional
    public Hilo crearHiloConPrimerMensaje(CrearHiloDTO dto, List<AdjuntoData> adjuntosData, Atleta atleta, Entrenador entrenador, Usuario emisor) {
        // 1. Crear el Hilo
        Hilo hilo = new Hilo();
        hilo.setId(UUID.randomUUID());
        hilo.setTitulo(dto.titulo());
        hilo.setCategoria(dto.categoria());
        hilo.setContexto(dto.contexto());
        hilo.setAtleta(atleta);
        hilo.setEntrenador(entrenador);
        hilo.setCreadoPor(emisor.getRol().name());

        // 2. Crear Mensaje Inicial
        Mensaje mensaje = new Mensaje();
        mensaje.setTexto(dto.texto());
        mensaje.setEnviadoPor(emisor.getRol().name());
        mensaje.setHilo(hilo);
        mensaje.setEnviadoEn(LocalDateTime.now());

        // 3. Vincular Adjuntos desde los metadatos de S3
        adjuntosData.forEach(data -> {
            Adjunto entidad = new Adjunto();
            entidad.setS3Key(data.s3Key());
            entidad.setNombreOriginal(data.nombreOriginal());
            entidad.setTipo(data.tipo());
            entidad.setMensaje(mensaje);
            mensaje.getAdjuntos().add(entidad);
        });

        hilo.getMensajes().add(mensaje);

        // 4. Guardar todo (CascadeType.ALL en Hilo guardará mensajes y adjuntos)
        Hilo guardado = hiloRepository.save(hilo);

        // 5. Actualizar Lectura
        actualizarEstadoLectura(guardado, emisor);

        return guardado;
    }
}
