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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/comunicacion")
@RequiredArgsConstructor
public class ComunicacionController {
    private final HiloService hiloService;

    @PostMapping(value = "/hilos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<HiloDetalleDTO> crearHilo(
            @RequestPart("datos") @Valid CrearHiloDTO dto,
            @RequestPart(value = "archivos", required = false) List<MultipartFile> archivos,
            @AuthenticationPrincipal UserPrincipal emisor
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(hiloService.crearHilo(dto, archivos, emisor.getId()));
    }

    @GetMapping("/hilos")
    public ResponseEntity<List<HiloResumenDTO>> listarHilos(
            @RequestParam UUID atletaId,
            @RequestParam ContextoHilo contexto,
            @AuthenticationPrincipal UserPrincipal usuario
    ) {
        return ResponseEntity.ok(hiloService.obtenerHilosPorAtleta(atletaId, contexto, usuario.getId()));
    }

    @GetMapping("/hilos/{id}")
    public ResponseEntity<HiloDetalleDTO> verDetalle(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal usuario
    ) {
        return ResponseEntity.ok(hiloService.obtenerDetalleHilo(id, usuario.getId()));
    }

    @PostMapping(value = "/hilos/{id}/mensajes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MensajeDTO> responder(
            @PathVariable UUID id,
            @RequestPart("texto") String texto,
            @RequestPart(value = "archivos", required = false) List<MultipartFile> archivos,
            @AuthenticationPrincipal UserPrincipal usuario
    ) {
        return ResponseEntity.ok(hiloService.responderHilo(id, texto, archivos, usuario.getId()));
    }
}