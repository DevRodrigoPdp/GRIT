package grit.sistema.backend.service.communication.impl;

import grit.sistema.backend.dto.communication.AdjuntoData;
import grit.sistema.backend.dto.communication.CrearHiloDTO;
import grit.sistema.backend.entity.Usuario;
import grit.sistema.backend.entity.coaching.Atleta;
import grit.sistema.backend.entity.coaching.Entrenador;
import grit.sistema.backend.entity.communication.*;
import grit.sistema.backend.mapper.communication.MensajeMapper;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MensajeServiceImpl implements MensajeService {
    private final MensajeRepository mensajeRepository;
    private final HiloRepository hiloRepository;
    private final UsuarioRepository usuarioRepository;
    private final LecturaHiloRepository lecturaRepository;
    private final MensajeMapper mensajeMapper;

    @Override
    @Transactional
    public Mensaje salvarMensaje(UUID hiloId, String texto, List<AdjuntoData> adjuntosData, UUID emisorId) {
        Hilo hilo = hiloRepository.findById(hiloId)
                .orElseThrow(() -> new EntityNotFoundException("Hilo no encontrado"));

        Usuario emisor = usuarioRepository.getReferenceById(emisorId);

        Mensaje mensaje = mensajeMapper.toEntity(texto, emisor.getRol().name(), adjuntosData);

        mensaje.setHilo(hilo);

        actualizarEstadoLectura(hilo, emisor);
        return mensajeRepository.save(mensaje);
    }

    @Override
    @Transactional
    public Hilo crearHiloConPrimerMensaje(CrearHiloDTO dto, List<AdjuntoData> adjuntosData, Atleta atleta, Entrenador entrenador, Usuario emisor) {
        Hilo hilo = Hilo.builder()
                .titulo(dto.titulo())
                .categoria(dto.categoria())
                .contexto(dto.contexto())
                .atleta(atleta)
                .entrenador(entrenador)
                .creadoPor(emisor.getRol().name())
                .mensajes(new ArrayList<>())
                .build();

        Mensaje mensajeInicial = mensajeMapper.toEntity(dto.texto(), emisor.getRol().name(), adjuntosData);

        mensajeInicial.setHilo(hilo);
        hilo.getMensajes().add(mensajeInicial);

        Hilo guardado = hiloRepository.save(hilo);

        actualizarEstadoLectura(guardado, emisor);
        return guardado;
    }


    private void actualizarEstadoLectura(Hilo hilo, Usuario usuario) {
        LecturaHilo lectura = lecturaRepository.findById(new LecturaHiloId(hilo.getId(), usuario.getId()))
                .orElse(new LecturaHilo(hilo, usuario));
        lectura.setLeidoEn(LocalDateTime.now());
        lecturaRepository.save(lectura);
    }
}
