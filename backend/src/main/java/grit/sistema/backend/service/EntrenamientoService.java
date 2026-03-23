package com.sistema.gritfitprueba.service;

import com.sistema.gritfitprueba.dto.EntrenamientoRequestDTO;
import com.sistema.gritfitprueba.dto.EntrenamientoResponseDTO;
import com.sistema.gritfitprueba.exception.AccesoDenegadoException;
import com.sistema.gritfitprueba.mapper.EntrenamientoMapper;
import com.sistema.gritfitprueba.model.Entrenamiento;
import com.sistema.gritfitprueba.model.Usuario;
import com.sistema.gritfitprueba.repositories.EntrenamientoRepository;
import com.sistema.gritfitprueba.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EntrenamientoService {
    private final EntrenamientoRepository entrenamientoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EntrenamientoMapper entrenamientoMapper;

    public EntrenamientoResponseDTO guardarEntrenamiento(EntrenamientoRequestDTO request, String email){
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Entrenamiento entrenamiento = entrenamientoMapper.toEntity(request, usuario);
        Entrenamiento guardado = entrenamientoRepository.save(entrenamiento);

        return entrenamientoMapper.toResponse(guardado);
    }

    public List<EntrenamientoResponseDTO> listarEntrenamientosPorUsuario(String email){
        return entrenamientoRepository.findAllByUsuarioEmail(email)
                .stream()
                .map(entrenamientoMapper::toResponse)
                .toList();
    }

    @Transactional
    public void eliminarEntrenamientoPorUuid(String uuid, String emailUsuarioAutenticado){
        Entrenamiento entrenamiento = entrenamientoRepository.findByUuid(UUID.fromString(uuid))
                .orElseThrow(() -> new RuntimeException("Entrenamiento no encontrado con UUID"));

        if(entrenamiento.getUsuario().getEmail().equals(emailUsuarioAutenticado)){
            throw new AccesoDenegadoException("No tienes permiso para borrar este entrenamiento. No te pertenece.");
        }
        entrenamientoRepository.delete(entrenamiento);
    }
}
