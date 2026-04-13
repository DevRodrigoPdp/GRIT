package grit.sistema.backend.security;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utilidad profesional para la generación de secretos criptográficos (Pepper).
 */
public class PepperGenerator {

    // Tamaño recomendado: 32 bytes (256 bits) para una seguridad robusta
    private static final int PEPPER_LENGTH = 32;

    public static String generatePepper() {
        // SecureRandom es criptográficamente fuerte, a diferencia de java.util.Random
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[PEPPER_LENGTH];

        // Llenamos el array con bytes aleatorios
        secureRandom.nextBytes(randomBytes);

        // Retornamos en Base64 para facilitar su manejo en archivos de texto
        return Base64.getEncoder().encodeToString(randomBytes);
    }

    public static void main(String[] args) {
        String newPepper = generatePepper();
        System.out.println("--- NUEVO PEPPER GENERADO ---");
        System.out.println(newPepper);
        System.out.println("-----------------------------");
        System.out.println("ADVERTENCIA: Guarda esto en tus variables de entorno, NUNCA en el código.");
    }
}
