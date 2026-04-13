package grit.sistema.backend.service;

import grit.sistema.backend.exception.FileStorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageService {

    private final S3Client s3Client;

    @Value("${application.storage.bucket-name}")
    private String bucketName;

    public String uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            // Error común de junior: No validar tipos de contenido (MIME types)
            throw new FileStorageException("No se puede subir un archivo vacío");
        }

        // Generar nombre único para evitar colisiones en el bucket
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename().replace(" ", "_");

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            // Usamos el InputStream directamente para eficiencia de memoria
            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            log.info("Archivo subido a MinIO por compensación: {}", fileName);
            return fileName;
        } catch (IOException e) {
            log.error("No se pudo subir el archivo {} de MinIO al leer el archivo: {}", fileName, e.getMessage());
            throw new FileStorageException("Error técnico al leer el flujo de datos", e);
        } catch (S3Exception e) {
            log.error("No se pudo subir el archivo {} de MinIO por problema: {}", fileName, e.getMessage());
            throw new FileStorageException("Error en la comunicación con el servidor de almacenamiento", e);
        }
    }

    // En StorageService.java
    public void deleteFile(String fileName) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build());
            log.info("Archivo eliminado de MinIO por compensación: {}", fileName);
        } catch (S3Exception e) {
            log.error("No se pudo eliminar el archivo {} de MinIO: {}", fileName, e.getMessage());
            // En un entorno real, aquí podrías enviar esto a una cola de reintentos
        }
    }
}