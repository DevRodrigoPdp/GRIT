package grit.sistema.backend.security.jwt.generator;

public final class EncryptionConstants {
    public static final int PEPPER_BYTE_LENGTH = 32; // 256 bits
    public static final String SECURE_RANDOM_ALGO = "DRBG"; // NIST SP 800-90A

    private EncryptionConstants() {} // Evita instanciación
}