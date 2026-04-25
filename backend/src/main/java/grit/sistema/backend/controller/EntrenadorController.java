package grit.sistema.backend.controller;

import grit.sistema.backend.dto.ApiResponseDTO;
import grit.sistema.backend.dto.atleta.AtletaResumenDTO;
import grit.sistema.backend.dto.entrenador.EntrenadorPerfilDTO;
import grit.sistema.backend.dto.training.HistorialPesoDTO;
import grit.sistema.backend.security.UserPrincipal;
import grit.sistema.backend.service.EntrenadorService;
import grit.sistema.backend.service.PesoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/entrenador")
@Tag(name = "Entrenadores")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ENTRENADOR')")
@Slf4j
public class EntrenadorController {
    private final EntrenadorService entrenadorService;
    private final PesoService pesoService;

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

    @PostMapping("/atletas/{atletaId}/peso/solicitar")
    public ResponseEntity<ApiResponseDTO<String>> solicitar(@PathVariable UUID atletaId, @AuthenticationPrincipal UserPrincipal usuario) {
        pesoService.solicitarCheckin(atletaId, usuario.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDTO<>(true, "Solicitud creada correctamente",null));
    }

    @GetMapping("/atletas/{atletaId}/peso/pendiente")
    public ResponseEntity<ApiResponseDTO<Map<String, Boolean>>> checkPendiente(@PathVariable UUID atletaId) {
        boolean pendiente = pesoService.tieneSolicitudPendiente(atletaId);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "pendiente", Map.of("pendiente", pendiente)));
    }

    @GetMapping("/atletas/{atletaId}/peso/historial")
    public ResponseEntity<ApiResponseDTO<List<HistorialPesoDTO>>> getHistorialAtleta(@PathVariable UUID atletaId) {
        var data = pesoService.obtenerHistorialAtleta(atletaId);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Historial del atleta recuperado", data));
    }
}
