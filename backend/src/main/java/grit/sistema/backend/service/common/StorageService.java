package grit.sistema.backend.service.common;

import grit.sistema.backend.exception.infrastructure.FileStorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
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

        String fileName = null;
        try {
            byte[] finalBytes;
            String contentType = file.getContentType();
            String originalName = file.getOriginalFilename() != null ?
                    file.getOriginalFilename().replace(" ", "_") : "file";
            fileName = UUID.randomUUID() + "_" + originalName;

            // [Mejora Senior]: Si optimizamos, normalizamos nombre y content-type
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
            return fileName;
        } catch (IOException | S3Exception e) {
            log.error("Error al subir archivo {}: {}", fileName, e.getMessage());
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
    public String uploadProfilePhoto(MultipartFile file, String folder) {
        if (file.isEmpty()) throw new FileStorageException("La foto de perfil está vacía");

        // 1. Validar que sea estrictamente una imagen
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new FileStorageException("El archivo debe ser una imagen válida (JPG/PNG)");
        }

        // 2. Generar nombre único estructurado por carpetas
        String fileName = folder + "/" + UUID.randomUUID() + ".jpg";

        try {
            // 3. Optimización extrema para perfiles (Cuadrado 500x500 es el estándar)
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Thumbnails.of(file.getInputStream())
                    .size(500, 500)         // Tamaño estándar para perfiles
                    .crop(net.coobird.thumbnailator.geometry.Positions.CENTER) // Centramos el recorte
                    .outputQuality(0.80)    // Buena calidad, poco peso
                    .outputFormat("jpg")
                    .toOutputStream(outputStream);

            byte[] photoBytes = outputStream.toByteArray();

            // 4. Subida a S3/Minio
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType("image/jpeg")
                    // [Nota Senior]: Si tu bucket es privado por defecto,
                    // aquí podrías añadir el ACL público si MinIO lo permite,
                    // o simplemente confiar en la política de la carpeta.
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(photoBytes));

            log.info("Foto de perfil subida con éxito: {}", fileName);
            return fileName;

        } catch (IOException | S3Exception e) {
            log.error("Error crítico al procesar foto de perfil: {}", e.getMessage());
            throw new FileStorageException("No se pudo procesar la foto de perfil", e);
        }
    }

    public String uploadAtletaFoto(MultipartFile file) {
        return uploadProfilePhoto(file, "profiles/atletas");
    }

    public String uploadEntrenadorFoto(MultipartFile file) {
        return uploadProfilePhoto(file, "profiles/entrenadores");
    }

    /**
     * Elimina un archivo del bucket de forma definitiva.
     * @param objectKey La clave (key) única del archivo en S3/MinIO.
     */
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

    private byte[] optimizarImagen(MultipartFile file) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Configuramos Thumbnailator
        Thumbnails.of(file.getInputStream())
                .size(1280, 720)       // Redimensionamos a un máximo de HD
                .outputQuality(0.75)   // Reducimos calidad al 75% (ahorro masivo de espacio)
                .outputFormat("jpg")   // Normalizamos todo a JPG
                .toOutputStream(outputStream);

        return outputStream.toByteArray();
    }

    private void validarMimeType(String contentType) {
        List<String> validTypes = List.of("application/pdf", "image/jpeg", "image/png");
        if (!validTypes.contains(contentType)) {
            throw new FileStorageException("Tipo de archivo no permitido: " + contentType);
        }
    }
}