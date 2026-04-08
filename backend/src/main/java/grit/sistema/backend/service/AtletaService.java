package grit.sistema.backend.service;

import grit.sistema.backend.dto.AtletaRequestDTO;
import grit.sistema.backend.dto.AtletaResponseDTO;
import grit.sistema.backend.mapper.AtletaMapper;
import grit.sistema.backend.model.Atleta;
import grit.sistema.backend.model.enums.Rol;
import grit.sistema.backend.model.enums.TipoServicio;
import grit.sistema.backend.repositories.AtletaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AtletaService {
    private final AtletaRepository atletaRepository;
    private final AtletaMapper atletaMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${application.security.pepper}")
    private String pepper;

    @Transactional
    public AtletaResponseDTO registrarAtleta(AtletaRequestDTO dto) {

        if (atletaRepository.findByEmail(dto.email()).isPresent()) {
            throw new RuntimeException("El correo electrónico ya está registrado");
        }

        Atleta atleta = atletaMapper.toEntity(dto);

        validarObjetivoSegunServicio(dto, atleta);

        String passwordWithPepper = dto.password() + pepper;

        atleta.setPassword(passwordEncoder.encode(passwordWithPepper));

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
