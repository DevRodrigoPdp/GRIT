package grit.sistema.backend.service.coaching;

import grit.sistema.backend.dto.coaching.AtletaRequestDTO;
import grit.sistema.backend.dto.coaching.AtletaResponseDTO;
import grit.sistema.backend.exception.business.UsuarioExistenteException;
import grit.sistema.backend.mapper.coaching.AtletaMapper;
import grit.sistema.backend.entity.coaching.Atleta;
import grit.sistema.backend.entity.common.enums.Rol;
import grit.sistema.backend.entity.coaching.enums.TipoServicio;
import grit.sistema.backend.repository.coaching.AtletaRepository;
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
    public AtletaResponseDTO guardarAtleta(AtletaRequestDTO dto, String fotoKey) {
        if (atletaRepository.existsByEmail(dto.email())) {
            throw new UsuarioExistenteException("EMAIL_DUPLICADO");
        }

        Atleta atleta = atletaMapper.toEntity(dto);
        validarObjetivoSegunServicio(dto, atleta);

        atleta.setPassword(passwordEncoder.encode(dto.password()));
        atleta.setRol(Rol.ATLETA);
        atleta.setFotoUrl(fotoKey);

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
