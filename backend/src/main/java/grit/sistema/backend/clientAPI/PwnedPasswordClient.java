package grit.sistema.backend.clientAPI;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
@Slf4j
@RequiredArgsConstructor
public class PwnedPasswordClient {
    private final RestTemplate pwnedRestTemplate;
    private static final String HIBP_API_URL = "https://api.pwnedpasswords.com/range/";
    private static final HexFormat HEX_FORMATTER = HexFormat.of().withUpperCase();

    public boolean isPasswordPwned(String password) {
        try {
            String sha1Hex = digest(password);
            String prefix = sha1Hex.substring(0, 5);
            String suffix = sha1Hex.substring(5);

            String response = pwnedRestTemplate.getForObject(HIBP_API_URL + prefix, String.class);

            return response != null && response.contains(suffix);
        } catch (Exception e) {
            log.error("Error al consultar HIBP: {}", e.getMessage());
            return false;
        }
    }

    private String digest(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            return HEX_FORMATTER.formatHex(md.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error crítico: Algoritmo no disponible", e);
        }
    }
}
