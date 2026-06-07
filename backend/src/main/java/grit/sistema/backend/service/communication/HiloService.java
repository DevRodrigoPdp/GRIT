package grit.sistema.backend.service.communication;

import grit.sistema.backend.dto.communication.CrearHiloDTO;
import grit.sistema.backend.dto.communication.HiloDetalleDTO;
import grit.sistema.backend.dto.communication.HiloResumenDTO;
import grit.sistema.backend.dto.communication.MensajeDTO;
import grit.sistema.backend.entity.communication.enums.ContextoHilo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface HiloService {

    /**
     * Crea un nuevo hilo con su primer mensaje y adjuntos opcionales.
     */
    HiloDetalleDTO crearHilo(CrearHiloDTO dto, List<MultipartFile> archivos, UUID emisorId);

    /**
     * Obtiene los hilos de un atleta filtrados por contexto (ENTRENAMIENTO/NUTRICION).
     * Devuelve una proyección o DTO para el dashboard.
     */
    List<HiloResumenDTO> obtenerHilosPorAtleta(UUID atletaId, ContextoHilo contexto);

    List<HiloResumenDTO> obtenerHilosParaEntrenador(UUID atletaId, UUID entrenadorId, ContextoHilo contexto);

    /**
     * Obtiene un hilo completo por su ID, marcándolo como leído para el usuario.
     */
    HiloDetalleDTO obtenerDetalleHilo(UUID hiloId, UUID usuarioAutenticadoId);

    /**
     * Añade un nuevo mensaje a un hilo existente.
     */
    MensajeDTO responderHilo(UUID hiloId, String texto, List<MultipartFile> archivos, UUID emisorId);

    /**
     * Marca un hilo como leído manualmente.
     */
    void marcarComoLeido(UUID hiloId, UUID usuarioId);
}
