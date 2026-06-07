package grit.sistema.backend.security;

import java.security.SecureRandom;
import java.util.Base64;

public class PepperGenerator {
    public static void main(String[] args) {
        byte[] pepper = new byte[32]; // 256 bits
        new SecureRandom().nextBytes(pepper);
        String encodedPepper = Base64.getEncoder().encodeToString(pepper);
        System.out.println("Tu Pepper seguro: " + encodedPepper);
    }
}