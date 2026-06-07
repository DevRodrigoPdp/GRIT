package grit.sistema.backend.service.common;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String uploadFile(MultipartFile file);
    String uploadProfilePhoto(MultipartFile file, String folder);
    String uploadAtletaFoto(MultipartFile file);
    String uploadEntrenadorFoto(MultipartFile file);
    void deleteFile(String objectKey);
    String getPresignedUrl(String objectKey);
}