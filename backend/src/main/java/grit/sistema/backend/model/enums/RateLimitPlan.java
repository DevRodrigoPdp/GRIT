package grit.sistema.backend.model.enums;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;

import java.time.Duration;

public enum RateLimitPlan {
    ADMIN(100), // 100 peticiones por minuto
    ENTRENADOR(20),   // 20 peticiones por minuto
    ATLETA(20),   // 20 peticiones por minuto
    FREE(5);    // 5 peticiones por minuto

    private final int bucketCapacity;

    RateLimitPlan(int bucketCapacity) {
        this.bucketCapacity = bucketCapacity;
    }

    // Definimos el límite: 'bucketCapacity' créditos que se recargan cada minuto
    public Bandwidth getLimit() {
        return Bandwidth.classic(bucketCapacity, Refill.intervally(bucketCapacity, Duration.ofMinutes(1)));
    }

    public static RateLimitPlan resolvePlanFromRole(String role) {
        if (role == null) return FREE;
        return switch (role) {
            case "ROLE_ADMIN" -> ADMIN;
            case "ROLE_ENTRENADOR" -> ENTRENADOR;
            case "ROLE_ATLETA" -> ATLETA;
            default -> FREE;
        };
    }
}
