package grit.sistema.backend.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import grit.sistema.backend.entity.common.enums.RateLimitPlan;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitService {

    // Cache de Caffeine: Expira 1 hora después del último acceso
    private final Cache<String, Bucket> cache;

    public RateLimitService() {
        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofHours(1))
                .maximumSize(10_000) // Límite de seguridad para evitar llenar la RAM
                .build();
    }

    public Bucket resolveBucket(String key, RateLimitPlan plan) {
        // .get(key, mappingFunction) es atómico y thread-safe
        return cache.get(key, k -> Bucket.builder()
                .addLimit(plan.getLimit())
                .build());
    }
}