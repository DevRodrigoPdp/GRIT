package grit.sistema.backend.service.common.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import grit.sistema.backend.entity.common.enums.RateLimitPlan;
import grit.sistema.backend.service.common.RateLimitService;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitServiceImpl implements RateLimitService {

    private final Cache<String, Bucket> cache;

    public RateLimitServiceImpl() {
        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofHours(1))
                .maximumSize(10_000)
                .build();
    }

    @Override
    public boolean tryConsume(String key, RateLimitPlan plan) {
        Bucket bucket = cache.get(key, k -> Bucket.builder()
                .addLimit(plan.getLimit())
                .build());

        return bucket.tryConsume(1);
    }
}
