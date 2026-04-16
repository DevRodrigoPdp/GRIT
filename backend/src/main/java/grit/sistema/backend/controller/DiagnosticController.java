package grit.sistema.backend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Diagnóstico")
@RequestMapping("/api/v1/diagnostic")
public class DiagnosticController {

    @GetMapping("/check-threads")
    public String checkThreads(){
        Thread currentThread = Thread.currentThread();
        boolean isVirtual = currentThread.isVirtual();

        return String.format("Tread Name: %s | isVirtual: %b", currentThread.getName(), isVirtual);
    }
}
