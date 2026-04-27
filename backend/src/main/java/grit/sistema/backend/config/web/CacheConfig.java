package grit.sistema.backend.config.web;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // 1. Configuración para Seguridad (TTL corto: 15 min)
        // Por si baneamos a alguien, que no tarde mucho en surtir efecto
        cacheManager.registerCustomCache("usuariosSecurity",
                Caffeine.newBuilder()
                        .expireAfterWrite(15, TimeUnit.MINUTES)
                        .maximumSize(500)
                        .build());

        // 2. Configuración para Perfiles y Rutinas (TTL largo: 60 min)
        // Son datos que no cambian cada minuto
        Caffeine<Object, Object> longLivedCache = Caffeine.newBuilder()
                .expireAfterWrite(60, TimeUnit.MINUTES)
                .maximumSize(1000);

        cacheManager.registerCustomCache("perfilesAtletas", longLivedCache.build());
        cacheManager.registerCustomCache("rutinasActivas", longLivedCache.build());

        return cacheManager;
    }
}