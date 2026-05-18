package grit.sistema.backend.service.common;

import grit.sistema.backend.entity.common.enums.RateLimitPlan;

public interface RateLimitService {
    boolean tryConsume(String key, RateLimitPlan plan);
}