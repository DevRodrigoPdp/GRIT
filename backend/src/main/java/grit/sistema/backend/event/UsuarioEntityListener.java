package grit.sistema.backend.event;

import grit.sistema.backend.dto.common.ArchivosAEliminarEventDTO;
import grit.sistema.backend.service.common.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class UsuarioEntityListener {

    private final StorageService storageService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCleanup(ArchivosAEliminarEventDTO event) {
        log.info("DB Commit exitoso. Iniciando limpieza de {} archivos en S3...", event.keys().size());
        event.keys().forEach(storageService::deleteFile);
    }
}
