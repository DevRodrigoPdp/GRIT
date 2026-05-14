package grit.sistema.backend.service.coaching.impl;

import grit.sistema.backend.clientAPI.PwnedPasswordClient;
import grit.sistema.backend.dto.coaching.*;
import grit.sistema.backend.entity.coaching.Asignacion;
import grit.sistema.backend.entity.coaching.Atleta;
import grit.sistema.backend.entity.coaching.Entrenador;
import grit.sistema.backend.entity.coaching.enums.NivelAtleta;
import grit.sistema.backend.entity.coaching.enums.Objetivo;
import grit.sistema.backend.entity.coaching.enums.TipoServicio;
import grit.sistema.backend.exception.security.PwnedPasswordException;
import grit.sistema.backend.mapper.coaching.AtletaMapper;
import grit.sistema.backend.repository.coaching.AsignacionRepository;
import grit.sistema.backend.repository.coaching.AtletaRepository;
import grit.sistema.backend.service.coaching.AtletaPersistenceService;
import grit.sistema.backend.service.coaching.AtletaService;
import grit.sistema.backend.service.common.StorageService;
import grit.sistema.backend.service.user.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AtletaServiceImpl implements AtletaService {
    private final AtletaRepository atletaRepository;
    private final AsignacionRepository asignacionRepository;
    private final UsuarioService usuarioService;
    private final PwnedPasswordClient pwnedClient;
    private final StorageService storageService;
    private final AtletaMapper atletaMapper;
    private final AtletaPersistenceService persistenceService;

    @Override
    @CacheEvict(value = "perfilAtleta", key = "#dto.email")
    public AtletaResponseDTO registrarAtleta(AtletaRequestDTO dto, MultipartFile foto) {
        if (pwnedClient.isPasswordPwned(dto.password())) {
            throw new PwnedPasswordException("Seguridad insuficiente: Contraseña detectada en filtraciones de datos.");
        }

        String fotoKey = subirFotoPerfil(foto);

        return persistenceService.guardarAtleta(dto, fotoKey);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "perfilAtleta", key = "#atletaId")
    public AtletaPerfilDTO obtenerPerfil(UUID atletaId) {
        return atletaRepository.findById(atletaId)
                .map(atletaMapper::toPerfilDTO)
                .orElseThrow(() -> new EntityNotFoundException("Atleta no encontrado"));
    }

    @Override
    @Transactional
    @CacheEvict(value = "perfilAtleta", key = "#atletaId")
    public AtletaPerfilDTO editarPerfil(UUID atletaId, AtletaEditarPerfilDTO request){
        Atleta atleta = atletaRepository.findById(atletaId)
                .orElseThrow(() -> new EntityNotFoundException("Atleta no encontrado"));

        atleta.setDeporte(request.deporte());
        atleta.setNivel(NivelAtleta.valueOf(request.nivel()));
        atleta.setAlturaCm(request.altura());
        atleta.setObjetivo(Objetivo.valueOf(request.objetivo()));
        atleta.setPesoKg(BigDecimal.valueOf(request.peso()));

        Atleta atletaActual = atletaRepository.save(atleta);
        return atletaMapper.toPerfilDTO(atletaActual);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfesionalAsignadoDTO> getProfesionalesAsignados(UUID atletaId) {
        List<Asignacion> asignaciones = asignacionRepository.findAsignacionesActivas(atletaId);
        List<ProfesionalAsignadoDTO> resultado = new ArrayList<>();

        for (Asignacion asig : asignaciones) {
            Entrenador e = asig.getEntrenador();
            TipoServicio servicio = asig.getTipoServicio();

            // Lógica para Entrenamiento
            if (servicio == TipoServicio.ENTRENAMIENTO || servicio == TipoServicio.AMBOS) {
                String titulo = (e.getTitulacionEntrenamiento() != null)
                        ? e.getTitulacionEntrenamiento().name()
                        : "Certificación Profesional";

                resultado.add(new ProfesionalAsignadoDTO(e.getId(), e.getNombre(), titulo, "ENTRENADOR", e.getDescripcion()));
            }

            // Lógica para Nutrición
            if (servicio == TipoServicio.NUTRICION || servicio == TipoServicio.AMBOS) {
                String titulo = (e.getTitulacionNutricion() != null)
                        ? e.getTitulacionNutricion().name()
                        : "Dietista Registrado";

                resultado.add(new ProfesionalAsignadoDTO(e.getId(), e.getNombre(), titulo, "NUTRICIONISTA", e.getDescripcion()));
            }
        }
        return resultado;
    }

    @Override
    @Transactional
    @CacheEvict(value = "perfilAtleta", key = "#atletaId")
    public void solicitarBajaCuenta(UUID atletaId) {
        Atleta atleta = atletaRepository.findById(atletaId)
                .orElseThrow(() -> new EntityNotFoundException("Entrenador no encontrado"));

        asignacionRepository.desactivarAsignacionesPorAtleta(atleta.getId());

        usuarioService.suspenderUsuario(atletaId);
    }

    private String subirFotoPerfil(MultipartFile foto) {
        return Optional.ofNullable(foto)
                .filter(f -> !f.isEmpty())
                .map(storageService::uploadAtletaFoto)
                .orElse(null);
    }
}
