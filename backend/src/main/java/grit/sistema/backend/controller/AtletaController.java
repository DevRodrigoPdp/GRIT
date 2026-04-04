package grit.sistema.backend.controller;

import grit.sistema.backend.dto.AtletaRequestDTO;
import grit.sistema.backend.dto.AtletaResponseDTO;
import grit.sistema.backend.model.Atleta;
import grit.sistema.backend.service.AtletaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/atletas")
@RequiredArgsConstructor
public class AtletaController {

    private final AtletaService atletaService;

    @PostMapping("/registro")
    public ResponseEntity<AtletaResponseDTO> registrar(@Valid @RequestBody AtletaRequestDTO dto){
        AtletaResponseDTO repuesta = atletaService.registrarAtleta(dto);
        return new ResponseEntity<>(repuesta, HttpStatus.CREATED);
    }
}
