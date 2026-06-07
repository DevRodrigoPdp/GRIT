package grit.sistema.backend.initializer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

@Slf4j
@Configuration
public class BucketInitializer {

    @Bean
    CommandLineRunner setupBucket(S3Client s3Client, @Value("${application.storage.bucket-name}") String bucketName) {
        return args -> {
            try {
                // Intentamos verificar si el bucket ya existe
                s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
                log.info("El bucket ya existe: {}", bucketName);
            } catch (NoSuchBucketException e) {
                // Si no existe, lo creamos
                s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
                log.info("Bucket creado exitosamente: {}", bucketName);
            } catch (Exception e) {
                // Un senior siempre imprime la causa real para poder debugear
                log.error("Error al inicializar el storage: {}", e.getMessage());
                // e.printStackTrace(); // Descomenta esto para ver el error completo (Timeout, Auth, etc)
            }
        };
    }
}
