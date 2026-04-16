package grit.sistema.backend.service;

import grit.sistema.backend.dto.atleta.AtletaPerfilDTO;
import grit.sistema.backend.dto.atleta.AtletaRequestDTO;
import grit.sistema.backend.dto.atleta.AtletaResponseDTO;
import grit.sistema.backend.dto.entrenador.EntrenadorPerfilDTO;
import grit.sistema.backend.mapper.AtletaMapper;
import grit.sistema.backend.model.coaching.Atleta;
import grit.sistema.backend.model.enums.Rol;
import grit.sistema.backend.model.enums.TipoServicio;
import grit.sistema.backend.repository.AtletaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class AtletaService {
    private final AtletaRepository atletaRepository;
    private final AtletaMapper atletaMapper;
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
}
