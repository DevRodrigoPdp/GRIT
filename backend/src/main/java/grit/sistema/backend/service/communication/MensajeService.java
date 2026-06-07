package grit.sistema.backend.service.communication;

import grit.sistema.backend.dto.communication.AdjuntoData;
import grit.sistema.backend.dto.communication.CrearHiloDTO;
import grit.sistema.backend.entity.Usuario;
import grit.sistema.backend.entity.coaching.Atleta;
import grit.sistema.backend.entity.coaching.Entrenador;
import grit.sistema.backend.entity.communication.Hilo;
import grit.sistema.backend.entity.communication.Mensaje;

import java.util.List;
import java.util.UUID;

public interface MensajeService {
    Mensaje salvarMensaje(UUID hiloId, String texto, List<AdjuntoData> adjuntos, UUID emisorId);

    Hilo crearHiloConPrimerMensaje(CrearHiloDTO dto, List<AdjuntoData> adjuntosData, Atleta atleta, Entrenador entrenador, Usuario emisor);
}
