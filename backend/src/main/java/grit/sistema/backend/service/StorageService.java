package grit.sistema.backend.service;

import grit.sistema.backend.exception.FileStorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageService {
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${application.storage.bucket-name}")
    private String bucketName;

    /**
     * Sube un archivo validando su integridad.
     */
    public String uploadFile(MultipartFile file) {
        if (file.isEmpty()) throw new FileStorageException("Archivo vacío");

        // [Mejora Senior]: Validar que sea PDF o imagen antes de subir
        validarMimeType(file.getContentType());

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename().replace(" ", "_");

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            return fileName;
        } catch (IOException | S3Exception e) {
            log.error("Error al subir archivo {}: {}", fileName, e.getMessage());
            throw new FileStorageException("Error en el almacenamiento persistente", e);
        }
    }

    /**
     * Elimina un archivo del bucket de forma definitiva.
     * @param objectKey La clave (key) única del archivo en S3/MinIO.
     */
    public void deleteFile(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            log.warn("Se intentó eliminar un archivo con nombre nulo o vacío");
            return;
        }

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("Archivo eliminado correctamente del almacenamiento: {}", objectKey);

        } catch (S3Exception e) {
            // [Nota Senior]: No lanzamos excepción para no romper la transacción de BD,
            // pero logueamos con ERROR para que salte en nuestros sistemas de monitoreo.
            log.error("Error crítico al eliminar el archivo {} de S3: {}", objectKey, e.awsErrorDetails().errorMessage());

            // Opcional: Podrías insertar esto en una tabla de "limpieza_pendiente"
            // para que un proceso programado (Cron/Job) lo intente borrar más tarde.
        }
    }

    /**
     * Genera una URL temporal de 15 minutos.
     * Este es el único método que el AdminService debería llamar.
     */
    public String getPresignedUrl(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) return null;

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(15))
                    .getObjectRequest(getObjectRequest)
                    .build();

            return s3Presigner.presignGetObject(presignRequest).url().toString();
        } catch (Exception e) {
            log.error("Error generando URL firmada para {}: {}", objectKey, e.getMessage());
            return null;
        }
    }

    private void validarMimeType(String contentType) {
        List<String> validTypes = List.of("application/pdf", "image/jpeg", "image/png");
        if (!validTypes.contains(contentType)) {
            throw new FileStorageException("Tipo de archivo no permitido: " + contentType);
        }
    }
}