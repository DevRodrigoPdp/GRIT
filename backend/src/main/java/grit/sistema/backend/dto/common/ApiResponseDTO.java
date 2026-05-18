package grit.sistema.backend.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // No envía campos nulos al JSON
public class ApiResponseDTO<T> {
    private boolean ok;
    private String message;
    private T data;

    // Método estático para respuestas de éxito rápidas
    public static <T> ApiResponseDTO<T> success(T data, String message) {
        return ApiResponseDTO.<T>builder()
                .ok(true)
                .message(message)
                .data(data)
                .build();
    }
}
