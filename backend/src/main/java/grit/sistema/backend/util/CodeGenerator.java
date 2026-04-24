package grit.sistema.backend.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.stream.Collectors;

@Component
public class CodeGenerator {
    private static final String ALPHA_NUMERIC = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // Excluimos 0, O, 1, I por legibilidad
    private static final SecureRandom random = new SecureRandom();

    public String generate(int length) {
        return random.ints(length, 0, ALPHA_NUMERIC.length())
                .mapToObj(ALPHA_NUMERIC::charAt)
                .map(Object::toString)
                .collect(Collectors.joining());
    }

    public String generateGritFormat() {
        // Genera formato GRIT-XXXX-XXXX
        return "GRIT-" + generate(4) + "-" + generate(4);
    }
}
