package grit.sistema.backend.security.jwt.generator;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Base64;

public class SecretGeneratorUtility {

    public static void main(String[] args) {
        // 1. Instanciamos el servicio de Pepper (en un main no hay inyección de Spring)
        PepperService pepperService = new PepperService();

        // 2. Generamos el Pepper para el hashing de contraseñas
        String strongPepper = pepperService.generateSecurePepper();

        // 3. Generamos la clave de firma para JWT (HS512 para máxima seguridad)
        // Usamos HS512 que requiere una clave de 512 bits (64 bytes)
        SecretKey jwtKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        String base64JwtKey = Base64.getEncoder().encodeToString(jwtKey.getEncoded());

        System.out.println("===========================================================");
        System.out.println("🛡️ CONFIGURACIÓN DE SEGURIDAD GENERADA 🛡️");
        System.out.println("===========================================================");
        System.out.println("1. JWT_SECRET_KEY (Para variables de entorno):");
        System.out.println(base64JwtKey);
        System.out.println("\n2. PASSWORD_PEPPER (Para variables de entorno):");
        System.out.println(strongPepper);
        System.out.println("===========================================================");
        System.out.println("⚠️ ADVERTENCIA: No compartas estos valores ni los subas a Git.");
    }
}
