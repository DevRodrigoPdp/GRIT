package grit.sistema.backend.service;

import grit.sistema.backend.dto.atleta.AtletaRequestDTO;
import grit.sistema.backend.dto.atleta.AtletaResponseDTO;
import grit.sistema.backend.exception.UsuarioExistenteException;
import grit.sistema.backend.mapper.AtletaMapper;
import grit.sistema.backend.model.coaching.Atleta;
import grit.sistema.backend.model.enums.Rol;
import grit.sistema.backend.model.enums.TipoServicio;
import grit.sistema.backend.repository.AtletaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AtletaPersistenceService {
    private final AtletaRepository atletaRepository;
    private final PasswordEncoder passwordEncoder;
    private final AtletaMapper atletaMapper;

    @Transactional
    public AtletaResponseDTO guardarAtleta(AtletaRequestDTO dto) {
        if (atletaRepository.existsByEmail(dto.email())) {
            throw new UsuarioExistenteException("EMAIL_DUPLICADO");
        }

        Atleta atleta = atletaMapper.toEntity(dto);
        validarObjetivoSegunServicio(dto, atleta);

        atleta.setPassword(passwordEncoder.encode(dto.password()));
        atleta.setRol(Rol.ATLETA);

        Atleta atletaGuardado = atletaRepository.save(atleta);

        return atletaMapper.toResponseDTO(atletaGuardado);
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
