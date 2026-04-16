package grit.sistema.backend.controller;

import grit.sistema.backend.dto.ApiResponseDTO;
import grit.sistema.backend.dto.atleta.AtletaResumenDTO;
import grit.sistema.backend.dto.entrenador.EntrenadorPerfilDTO;
import grit.sistema.backend.service.EntrenadorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/entrenador")
@Tag(name = "Entrenadores")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ENTRENADOR')")
@Slf4j
public class EntrenadorController {
    private final EntrenadorService entrenadorService;

    @Operation(summary = "Ver perfil de entrenador")
    @GetMapping("/perfil")
    public ResponseEntity<ApiResponseDTO<EntrenadorPerfilDTO>> getPerfil(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Consultando perfil para el entrenador: {}", userDetails.getUsername());
        EntrenadorPerfilDTO perfilDTO = entrenadorService.obtenerPerfil(userDetails.getUsername());
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Perfil del entrenador", perfilDTO));
    }

    @Operation(summary = "Listar atletas asociados a un entrenador")
    @GetMapping("/atletas")
    public ResponseEntity<ApiResponseDTO<List<AtletaResumenDTO>>> getAtletas(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Consultando atletas del entrenador: {}", userDetails.getUsername());
        List<AtletaResumenDTO> listaAtletas = entrenadorService.listarMisAtletas(userDetails.getUsername());

        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Atletas del entrenador", listaAtletas));
    }
}
