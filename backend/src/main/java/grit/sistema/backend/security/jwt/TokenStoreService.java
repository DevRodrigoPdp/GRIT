package grit.sistema.backend.security.jwt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
public class TokenStoreService {

    private final Cache burnedJtisCache;
    private final long GRACE_PERIOD_SECONDS = 30;

    public TokenStoreService(CacheManager cacheManager) {
        // Obtenemos la abstracción de Spring Cache
        this.burnedJtisCache = cacheManager.getCache("burnedTokens");
    }

    public void burnJti(String jti) {
        if (jti != null && burnedJtisCache != null) {
            burnedJtisCache.put(jti, Instant.now());
        }
    }

    public boolean isJtiReusable(String jti) {
        if (jti == null || burnedJtisCache == null) return false;

        // Spring Cache devuelve un Wrapper (ValueWrapper) o el objeto mapeado
        Instant rotationTime = burnedJtisCache.get(jti, Instant.class);

        if (rotationTime == null) return true; // No está quemado

        return Instant.now().isBefore(rotationTime.plusSeconds(GRACE_PERIOD_SECONDS));
    }

    public boolean isReuseDetected(String jti) {
        if (jti == null || burnedJtisCache == null) return false;

        Instant rotationTime = burnedJtisCache.get(jti, Instant.class);

        // Detectamos reúso si ya pasó el margen de gracia
        return rotationTime != null &&
                Instant.now().isAfter(rotationTime.plusSeconds(GRACE_PERIOD_SECONDS));
    }
}
