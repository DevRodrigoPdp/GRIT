package grit.sistema.backend.controller.communication;

import grit.sistema.backend.dto.communication.CrearHiloDTO;
import grit.sistema.backend.dto.communication.HiloDetalleDTO;
import grit.sistema.backend.dto.communication.HiloResumenDTO;
import grit.sistema.backend.dto.communication.MensajeDTO;
import grit.sistema.backend.entity.communication.enums.ContextoHilo;
import grit.sistema.backend.security.model.UserPrincipal;
import grit.sistema.backend.service.communication.HiloService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/comunicacion")
@RequiredArgsConstructor
public class ComunicacionController {
    private final HiloService hiloService;

    @PreAuthorize("hasAnyRole('ATLETA', 'ENTRENADOR', 'ADMIN')")
    @PostMapping(value = "/hilos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<HiloDetalleDTO> crearHilo(
            @RequestPart("datos") @Valid CrearHiloDTO dto,
            @RequestPart(value = "archivos", required = false) List<MultipartFile> archivos,
            @AuthenticationPrincipal UserPrincipal emisor
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(hiloService.crearHilo(dto, archivos, emisor.getId()));
    }

    @PreAuthorize("hasRole('ATLETA')")
    @GetMapping("/atleta/hilos")
    public ResponseEntity<List<HiloResumenDTO>> listarMisHilos(
            @RequestParam ContextoHilo contexto,
            @AuthenticationPrincipal UserPrincipal usuario
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS).cachePrivate().mustRevalidate())
                .body(hiloService.obtenerHilosPorAtleta(usuario.getId(), contexto, usuario.getId()));
    }

    @PreAuthorize("hasRole('ENTRENADOR') and @asignacionService.esEntrenadorDeAtleta(authentication.principal.id, #atletaId)")
    @GetMapping("/entrenador/atleta/{atletaId}/hilos")
    public ResponseEntity<List<HiloResumenDTO>> listarHilosDeAtleta(
            @PathVariable UUID atletaId,
            @RequestParam ContextoHilo contexto,
            @AuthenticationPrincipal UserPrincipal entrenador
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS).cachePrivate().mustRevalidate())
                .body(hiloService.obtenerHilosParaEntrenador(atletaId, entrenador.getId(), contexto));
    }

    @PreAuthorize("hasAnyRole('ATLETA', 'ENTRENADOR')")
    @GetMapping("/hilos/{id}")
    public ResponseEntity<HiloDetalleDTO> verDetalle(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal usuario
    ) {
        return ResponseEntity.ok(hiloService.obtenerDetalleHilo(id, usuario.getId()));
    }

    @PreAuthorize("hasAnyRole('ATLETA', 'ENTRENADOR')")
    @PostMapping(value = "/hilos/{id}/mensajes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MensajeDTO> responder(
            @PathVariable UUID id,
            @RequestPart(value = "texto", required = false) String texto,
            @RequestPart(value = "archivos", required = false) List<MultipartFile> archivos,
            @AuthenticationPrincipal UserPrincipal usuario
    ) throws BadRequestException {
        if ((texto == null || texto.isBlank()) && (archivos == null || archivos.isEmpty())) {
            throw new BadRequestException("Debe enviar al menos un texto o un archivo");
        }

        return ResponseEntity.ok(hiloService.responderHilo(id, texto, archivos, usuario.getId()));
    }

    @PreAuthorize("hasAnyRole('ATLETA', 'ENTRENADOR')")
    @PatchMapping("/hilos/{id}/leer")
    public ResponseEntity<Void> marcarComoLeido(@PathVariable UUID id, @AuthenticationPrincipal UserPrincipal usuario) {
        hiloService.marcarComoLeido(id, usuario.getId());
        return ResponseEntity.ok().build();
    }
}