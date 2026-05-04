package grit.sistema.backend.repository.communication;

import grit.sistema.backend.dto.communication.HiloResumenDTO;
import grit.sistema.backend.entity.communication.Hilo;
import grit.sistema.backend.entity.communication.enums.ContextoHilo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface HiloRepository extends JpaRepository<Hilo, UUID> {
    @Query("""
        SELECT new grit.sistema.backend.dto.communication.HiloResumenDTO(
                h.id,\s
                h.titulo,\s
                h.categoria,\s
                h.contexto,\s
                h.creadoPor,\s
                h.creadoEn,
                (SELECT COUNT(m) FROM Mensaje m WHERE m.hilo = h),
                new grit.sistema.backend.dto.communication.UltimoMensajeDTO(
                    m_last.texto, m_last.enviadoEn, m_last.enviadoPor
                ),
                (l.leidoEn >= m_last.enviadoEn)
            )
        FROM Hilo h
        JOIN h.mensajes m_last
        LEFT JOIN LecturaHilo l ON l.hilo = h AND l.usuario.id = :usuarioId
        WHERE h.atleta.id = :atletaId
        AND h.contexto = :contexto
        AND m_last.enviadoEn = (SELECT MAX(m2.enviadoEn) FROM Mensaje m2 WHERE m2.hilo = h)
        ORDER BY m_last.enviadoEn DESC
    """)
    List<HiloResumenDTO> findResumenByAtletaAndContexto(
            @Param("atletaId") UUID atletaId,
            @Param("contexto") ContextoHilo contexto,
            @Param("usuarioId") UUID usuarioId
    );
}
