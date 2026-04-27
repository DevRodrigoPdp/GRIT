package grit.sistema.backend.security.jwt.generator;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.util.Base64;
import javax.crypto.SecretKey;

public class KeyGenerator {
    public static void main(String[] args) {
        // Genera una clave segura para HS256 (mínimo 256 bits / 32 bytes)
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

        // La codificamos en Base64 ESTÁNDAR para que sea segura en archivos de texto
        String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());

        System.out.println("Tu clave segura es: " + base64Key);
    }
}
