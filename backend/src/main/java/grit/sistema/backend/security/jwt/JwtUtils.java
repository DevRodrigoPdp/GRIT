package grit.sistema.backend.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtUtils {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration; // 900000 (15 min)

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration; // 604800000 (7 días)

    @Value("${application.security.cookie.secure}")
    private boolean isSecure;

    public ResponseCookie generateAccessCookie(String email, String rol) {
        String token = generarAccessToken(email, rol);
        return buildCookie("access_token", token, jwtExpiration / 1000, "/");
    }

    public ResponseCookie generateRefreshCookie(String email) {
        String token = generarRefreshToken(email);
        return buildCookie("refresh_token", token, refreshExpiration / 1000, "/api/v1/auth/refresh");
    }

    private ResponseCookie buildCookie(String name, String value, long maxAge, String path) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(isSecure)
                .sameSite("Strict")
                .path(path)
                .maxAge(maxAge)
                .build();
    }

    public ResponseCookie getCleanAccessCookie() {
        return buildCookie("access_token", "", 0, "/");
    }

    public ResponseCookie getCleanRefreshCookie() {
        return buildCookie("refresh_token", "", 0, "/api/v1/auth/refresh");
    }

    public String generarAccessToken(String email, String rol) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("rol", rol.startsWith("ROLE_") ? rol : "ROLE_" + rol);
        return construirToken(extraClaims, email, jwtExpiration);
    }

    public String generarRefreshToken(String email) {
        return construirToken(new HashMap<>(), email, refreshExpiration);
    }

    public String construirToken(Map<String, Object> extraClaims, String subject, long expiration) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extraerRol(String token){
        return extraerClaim(token, claims -> claims.get("rol", String.class));
    }

    public String extraerEmail(String token) {
        return extraerClaim(token, Claims::getSubject);
    }

    public <T> T extraerClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extraerAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extraerAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // --- VALIDACIÓN ---

    public boolean esTokenValido(String token, String emailUsuario) {
        final String email = extraerEmail(token);
        return (email.equals(emailUsuario) && !isTokenExpirado(token));
    }

    private boolean isTokenExpirado(String token) {
        return extraerClaim(token, Claims::getExpiration).before(new Date());
    }
}
