package grit.sistema.backend.controller.coaching;

import grit.sistema.backend.dto.auth.PasswordUpdateDTO;
import grit.sistema.backend.dto.coaching.*;
import grit.sistema.backend.dto.common.ApiResponseDTO;
import grit.sistema.backend.dto.training.HistorialPesoDTO;
import grit.sistema.backend.security.jwt.JwtUtils;
import grit.sistema.backend.security.model.UserPrincipal;
import grit.sistema.backend.service.coaching.AsignacionService;
import grit.sistema.backend.service.coaching.EntrenadorService;
import grit.sistema.backend.service.training.PesoService;
import grit.sistema.backend.service.user.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    private final AsignacionService asignacionService;
    private final PesoService pesoService;
    private final UsuarioService usuarioService;
    private final JwtUtils jwtUtils;

    @Operation(summary = "Ver perfil de entrenador")
    @GetMapping("/perfil")
    public ResponseEntity<ApiResponseDTO<EntrenadorPerfilDTO>> getPerfil(@AuthenticationPrincipal UserPrincipal usuario) {
        EntrenadorPerfilDTO perfilDTO = entrenadorService.obtenerPerfil(usuario.getId());
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Perfil del entrenador", perfilDTO));
    }

    @Operation(summary = "Actualizar perfil de entrenador")
    @PutMapping("/perfil")
    public ResponseEntity<ApiResponseDTO<EntrenadorPerfilDTO>> actualizarPerfil(@Valid @RequestBody EntrenadorEditarPerfilDTO dto, @AuthenticationPrincipal UserPrincipal usuario) {
        EntrenadorPerfilDTO perfilDTO = entrenadorService.editarPerfil(usuario.getId(), dto);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Perfil del entrenador actualizado", perfilDTO));
    }

    @Operation(summary = "Actualizar foto de perfil")
    @PatchMapping(value = "/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDTO<FotoPerfilResponseDTO>> actualizarFoto(
            @RequestPart("fotoPerfil") MultipartFile foto,
            @AuthenticationPrincipal UserPrincipal usuario) {
        FotoPerfilResponseDTO response = usuarioService.actualizarFotoPerfil(usuario.getId(), foto);

        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Foto actualizada exitosamente", response));
    }

    @Operation(summary = "Listar atletas asociados a un entrenador")
    @GetMapping("/atletas")
    public ResponseEntity<ApiResponseDTO<List<AtletaResumenDTO>>> getAtletas(@AuthenticationPrincipal UserPrincipal usuario) {
        List<AtletaResumenDTO> listaAtletas = entrenadorService.listarMisAtletas(usuario.getId());

        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Atletas del entrenador", listaAtletas));
    }

    @Operation(summary = "Darse de baja del atleta")
    @DeleteMapping("/atletas/{atletaId}/desconectar")
    public ResponseEntity<ApiResponseDTO<Void>> desconectarAtleta(@PathVariable UUID atletaId, @AuthenticationPrincipal UserPrincipal usuario) {
        asignacionService.terminarAsignacion(usuario.getId(), atletaId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Entrenador solicita ampliación de titulación.")
    @PostMapping(value = "/ampliar-formacion", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDTO<EntrenadorResponseDTO>> ampliarFormacion(@RequestPart("datos") @Valid AmpliarFormacionDTO dto, @RequestPart(value = "documentos", required = false) List<MultipartFile> documentos, @AuthenticationPrincipal UserPrincipal usuario) {
        EntrenadorResponseDTO response = entrenadorService.ampliarFormacion(usuario.getId(), dto, documentos);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Solicitud de ampliación de formación enviada", response));
    }

    @PostMapping("/atletas/{atletaId}/peso/solicitar")
    public ResponseEntity<ApiResponseDTO<String>> solicitar(@PathVariable UUID atletaId, @AuthenticationPrincipal UserPrincipal usuario) {
        pesoService.solicitarCheckin(atletaId, usuario.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDTO<>(true, "Solicitud creada correctamente", null));
    }

    @GetMapping("/atletas/{atletaId}/peso/pendiente")
    public ResponseEntity<ApiResponseDTO<Map<String, Boolean>>> checkPendiente(@PathVariable UUID atletaId) {
        boolean pendiente = pesoService.tieneSolicitudPendiente(atletaId);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "pendiente", Map.of("pendiente", pendiente)));
    }

    @PreAuthorize("hasRole('ENTRENADOR') and @asignacionService.esEntrenadorDeAtleta(authentication.principal.id, #atletaId)")
    @GetMapping("/atletas/{atletaId}/peso/historial")
    public ResponseEntity<ApiResponseDTO<List<HistorialPesoDTO>>> getHistorialAtleta(@PathVariable UUID atletaId) {
        var data = pesoService.obtenerHistorialAtleta(atletaId);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Historial del atleta recuperado", data));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(@Valid @RequestBody PasswordUpdateDTO dto, @AuthenticationPrincipal UserPrincipal usuario) {
        usuarioService.actualizarPassword(usuario.getId(), dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/cuenta")
    public ResponseEntity<Void> eliminarCuenta(@AuthenticationPrincipal UserPrincipal usuario) {

        entrenadorService.solicitarBajaCuenta(usuario.getId());

        ResponseCookie accessCookie = jwtUtils.getCleanAccessCookie();
        ResponseCookie refreshCookie = jwtUtils.getCleanRefreshCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .build();
    }
}
