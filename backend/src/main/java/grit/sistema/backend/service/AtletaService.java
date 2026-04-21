package grit.sistema.backend.service;

import grit.sistema.backend.dto.atleta.AtletaPerfilDTO;
import grit.sistema.backend.dto.atleta.AtletaRequestDTO;
import grit.sistema.backend.dto.atleta.AtletaResponseDTO;
import grit.sistema.backend.dto.entrenador.EntrenadorPerfilDTO;
import grit.sistema.backend.dto.training.EjercicioResponseDTO;
import grit.sistema.backend.dto.training.RutinaDTO;
import grit.sistema.backend.mapper.AtletaMapper;
import grit.sistema.backend.model.coaching.Atleta;
import grit.sistema.backend.model.enums.Rol;
import grit.sistema.backend.model.enums.TipoServicio;
import grit.sistema.backend.model.training.Rutina;
import grit.sistema.backend.repository.AtletaRepository;
import grit.sistema.backend.repository.RutinaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AtletaService {
    private final AtletaRepository atletaRepository;
    private final AtletaMapper atletaMapper;
    private final RutinaRepository rutinaRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AtletaResponseDTO registrarAtleta(AtletaRequestDTO dto) {

        if (atletaRepository.findByEmail(dto.email()).isPresent()) {
            throw new RuntimeException("El correo electrónico ya está registrado");
        }

        Atleta atleta = atletaMapper.toEntity(dto);

        validarObjetivoSegunServicio(dto, atleta);

        String passwordWithPepper = dto.password();

        atleta.setPassword(passwordEncoder.encode(passwordWithPepper));

        atleta.setRol(Rol.ATLETA);

        Atleta atletaGuardado = atletaRepository.save(atleta);

        return atletaMapper.toResponseDTO(atletaGuardado);
    }

    @Transactional(readOnly = true)
    public AtletaPerfilDTO obtenerPerfil(String email) {
        Atleta a = atletaRepository.findByEmail(email)
                .orElseThrow(()->new EntityNotFoundException("Atleta no encontrado"));

        return new AtletaPerfilDTO(
                a.getId(), a.getNombre(), a.getEmail(),
                a.getFechaNac(), a.getGenero(), a.getPesoKg(),
                a.getAlturaCm(), a.getDeporte(), a.getNivel(),
                a.getServicio(), a.getObjetivo()
        );
    }

    public RutinaDTO getPlanActivoAtleta(UUID atletaId) {
        return rutinaRepository.findFirstByAtletaIdOrderByCreadoEn(atletaId)
                .map(this::mapToRutinaDTO)
                .orElse(null); // El requerimiento pide null si no hay plan
    }

    private void validarObjetivoSegunServicio(AtletaRequestDTO dto, Atleta atleta) {
        boolean necesitaObjetivo = dto.servicio() == TipoServicio.ENTRENAMIENTO
                || dto.servicio() == TipoServicio.AMBOS;
        if (necesitaObjetivo && dto.objetivo() == null) {
            throw new IllegalArgumentException("OBJETIVO_REQUERIDO");
        }

        if (dto.servicio() == TipoServicio.NUTRICION) {
            atleta.setObjetivo(null);
        }
    }

    private RutinaDTO mapToRutinaDTO(Rutina rutina) {
        return new RutinaDTO(
                rutina.getId(),
                rutina.getAtletaId(),
                rutina.getNombre(),
                rutina.getDescripcion(),
                rutina.getCreadoEn(),
                rutina.getSesiones().stream().map(sesion -> new grit.sistema.backend.dto.training.SesionRutinaDTO(
                        sesion.getId(),
                        sesion.getNombre(),
                        sesion.getOrden(),
                        sesion.getEjercicios().stream().map(ejercicio -> new EjercicioResponseDTO(
                                ejercicio.getId(),
                                ejercicio.getEjercicioId(),
                                ejercicio.getEjercicioNombre(),
                                ejercicio.getSeries(),
                                ejercicio.getReps(),
                                ejercicio.getNotas(),
                                ejercicio.getOrden()
                        )).toList()
                )).toList()
        );
    }
}
