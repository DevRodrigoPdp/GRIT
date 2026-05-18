package grit.sistema.backend.security.jwt.generator;

import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class PepperService {

    /**
     * Genera un Pepper de alta entropía.
     * @return String codificado en Base64
     */
    public String generateSecurePepper() {
        try {
            // Intentamos usar el algoritmo más fuerte disponible (DRBG)
            SecureRandom sr = SecureRandom.getInstance(EncryptionConstants.SECURE_RANDOM_ALGO);
            byte[] bytes = new byte[EncryptionConstants.PEPPER_BYTE_LENGTH];
            sr.nextBytes(bytes);
            return Base64.getEncoder().encodeToString(bytes);
        } catch (NoSuchAlgorithmException e) {
            // Fallback a la instancia por defecto si el SO no soporta DRBG
            SecureRandom srDefault = new SecureRandom();
            byte[] bytes = new byte[EncryptionConstants.PEPPER_BYTE_LENGTH];
            srDefault.nextBytes(bytes);
            return Base64.getEncoder().encodeToString(bytes);
        }
    }

    /**
     * Verifica si un pepper existente cumple con los estándares de longitud.
     */
    public boolean isPepperStrong(String pepper) {
        if (pepper == null || pepper.isEmpty()) return false;
        try {
            byte[] decoded = Base64.getDecoder().decode(pepper);
            return decoded.length >= EncryptionConstants.PEPPER_BYTE_LENGTH;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}