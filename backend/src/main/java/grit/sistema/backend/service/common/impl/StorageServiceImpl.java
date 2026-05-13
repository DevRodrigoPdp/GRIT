package grit.sistema.backend.service.common.impl;

import grit.sistema.backend.exception.infrastructure.FileStorageException;
import grit.sistema.backend.service.common.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageServiceImpl implements StorageService {
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${application.storage.bucket-name}")
    private String bucketName;

    /**
     * Sube un archivo validando su integridad.
     */
    @Override
    public String uploadFile(MultipartFile file) {
        if (file.isEmpty()) throw new FileStorageException("Archivo vacío");

        // Validar que sea PDF o imagen antes de subir
        validarMimeType(file.getContentType());

        String fileName = null;
        try {
            log.info("Iniciando subida de archivo: {} (Tipo: {}, Tamaño: {} bytes)",
                    file.getOriginalFilename(), file.getContentType(), file.getSize());

            byte[] finalBytes;
            String contentType = file.getContentType();
            String originalName = file.getOriginalFilename() != null ?
                    file.getOriginalFilename().replace(" ", "_") : "file";
            fileName = UUID.randomUUID() + "_" + originalName;

            // Si optimizamos, normalizamos nombre y content-type
            if (contentType != null && contentType.startsWith("image/")) {
                finalBytes = optimizarImagen(file);
                contentType = "image/jpeg"; // Forzamos porque Thumbnailator saca JPG
                if (!fileName.toLowerCase().endsWith(".jpg") && !fileName.toLowerCase().endsWith(".jpeg")) {
                    fileName += ".jpg";
                }
            } else {
                finalBytes = file.getBytes();
            }

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(finalBytes));

            log.info("Archivo almacenado exitosamente en S3 con clave: {}", fileName);
            return fileName;

        } catch (IOException | S3Exception e) {
            throw new FileStorageException("Error en el almacenamiento persistente", e);
        }
    }

    /**
     * Sube una foto de perfil optimizada a una carpeta específica.
     * A diferencia de los documentos, estas suelen ser de acceso público.
     * * @param file El archivo de imagen desde el controlador.
     * @param folder Carpeta dentro del bucket (ej: "profiles/atleta")
     * @return El nombre del objeto guardado para persistir en la DB.
     */
    @Override
    public String uploadProfilePhoto(MultipartFile file, String folder) {
        if (file.isEmpty()) throw new FileStorageException("La foto de perfil está vacía");

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new FileStorageException("El archivo debe ser una imagen válida (JPG/PNG)");
        }

        String fileName = folder + "/" + UUID.randomUUID() + ".jpg";

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Thumbnails.of(file.getInputStream())
                    .size(500, 500)
                    .crop(net.coobird.thumbnailator.geometry.Positions.CENTER)
                    .outputQuality(0.80)
                    .outputFormat("jpg")
                    .toOutputStream(outputStream);

            byte[] photoBytes = outputStream.toByteArray();

            // 4. Subida a S3/Minio
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType("image/jpeg")
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(photoBytes));

            log.info("Foto de perfil subida con éxito: {}", fileName);
            return fileName;

        } catch (IOException | S3Exception e) {
            throw new FileStorageException("No se pudo procesar la foto de perfil", e);
        }
    }

    @Override
    public String uploadAtletaFoto(MultipartFile file) {
        return uploadProfilePhoto(file, "profiles/atletas");
    }

    @Override
    public String uploadEntrenadorFoto(MultipartFile file) {
        return uploadProfilePhoto(file, "profiles/entrenadores");
    }

    /**
     * Elimina un archivo del bucket de forma definitiva.
     * @param objectKey La clave (key) única del archivo en S3/MinIO.
     */
    @Override
    @Async("fileDeletionExecutor")
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
            log.error("Error crítico al eliminar el archivo {} de S3: {}", objectKey, e.awsErrorDetails().errorMessage());
        }
    }

    /**
     * Genera una URL temporal de 15 minutos.
     * Este es el único método que el AdminService debería llamar.
     */
    @Override
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

    private byte[] optimizarImagen(MultipartFile file) throws IOException {
        long startTime = System.currentTimeMillis();
        long originalSize = file.getSize();

        if (originalSize < 50 * 1024) {
            log.info("Archivo pequeño detectado ({} KB), saltando optimización.", originalSize / 1024);
            return file.getBytes();
        }

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            Thumbnails.of(file.getInputStream())
                    .size(1280, 720)
                    .outputQuality(0.75)
                    .outputFormat("jpg")
                    .toOutputStream(outputStream);

            byte[] optimizedBytes = outputStream.toByteArray();
            long duration = System.currentTimeMillis() - startTime;

            log.info("Optimización exitosa: {} KB -> {} KB en {} ms",
                    originalSize / 1024, optimizedBytes.length / 1024, duration);

            return optimizedBytes;

        } catch (Exception e) {
            log.warn("Fallo en optimización para {}: {}. Usando archivo original como fallback.",
                    file.getOriginalFilename(), e.getMessage());

            try {
                return file.getBytes();
            } catch (IOException ioe) {
                throw new FileStorageException("No se puede procesar el archivo", ioe);
            }
        }
    }

    private void validarMimeType(String contentType) {
        List<String> validTypes = List.of("application/pdf", "image/jpeg", "image/png");
        if (!validTypes.contains(contentType)) {
            throw new FileStorageException("Tipo de archivo no permitido: " + contentType);
        }
    }
}
