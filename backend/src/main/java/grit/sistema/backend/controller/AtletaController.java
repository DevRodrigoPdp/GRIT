package grit.sistema.backend.controller;

import grit.sistema.backend.dto.ApiResponseDTO;
import grit.sistema.backend.dto.atleta.AtletaPerfilDTO;
import grit.sistema.backend.service.AtletaService;
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

@RestController
@RequestMapping("/api/v1/atleta")
@Tag(name = "Atletas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ATLETA')")
@Slf4j
public class AtletaController {
    private final AtletaService atletaService;

    @Operation(summary = "Ver perfil del atleta")
    @GetMapping("/perfil")
    public ResponseEntity<ApiResponseDTO<AtletaPerfilDTO>> getPerfil(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Consultando perfil para el atleta: {}", userDetails.getUsername());
        AtletaPerfilDTO perfilDTO = atletaService.obtenerPerfil(userDetails.getUsername());
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Perfil del atleta", perfilDTO));
    }


}
