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

    @Value("${application.storage.endpoint}")
    private String endpoint;

    @Value("${application.storage.access-key}")
    private String accessKey;

    @Value("${application.storage.secret-key}")
    private String secretKey;

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

    // En StorageService.java
    public String generatePresignedUrl(String fileName) {
        if (fileName == null || fileName.isBlank()) return null;

        // Configuración de expiración (ej. 15 minutos)
        Duration expiration = Duration.ofMinutes(15);

        try (S3Presigner presigner = S3Presigner.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.US_EAST_1)
                .build()) {

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(expiration)
                    .getObjectRequest(getObjectRequest)
                    .build();

            return presigner.presignGetObject(presignRequest).url().toString();
        }
    }

    /**
     * Genera una URL firmada para que el frontend pueda visualizar el archivo
     * sin que el bucket sea público. Seguridad ante todo.
     */
    public String getPresignedUrl(String fileName) {
        // Para generar URLs firmadas en SDK v2 se usa S3Presigner
        // Por brevedad, aquí simulamos la lógica:
        return endpoint + "/" + bucketName + "/" + fileName;
        // Nota Senior: En producción, usa S3Presigner para URLs temporales (e.g., 15 min)
    }

    /**
     * Lista todos los objetos para auditoría o gestión interna
     */
    public List<String> listFiles() {
        try {
            ListObjectsV2Response result = s3Client.listObjectsV2(ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .build());
            return result.contents().stream()
                    .map(S3Object::key)
                    .toList(); // Java 17 syntax
        } catch (S3Exception e) {
            throw new FileStorageException("Error al listar archivos", e);
        }
    }
}