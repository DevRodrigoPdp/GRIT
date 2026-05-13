package grit.sistema.backend.repository.communication;

import grit.sistema.backend.dto.communication.HiloResumenDTO;
import grit.sistema.backend.entity.communication.Hilo;
import grit.sistema.backend.entity.communication.enums.ContextoHilo;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

public interface HiloRepository extends JpaRepository<Hilo, UUID> {
    @Query("""
                SELECT new grit.sistema.backend.dto.communication.HiloResumenDTO(
                        h.id,
                        h.titulo,
                        h.categoria,
                        h.contexto,
                        h.creadoPor,
                        h.creadoEn,
                        CAST(COUNT(m) AS integer),
                        m_last.texto,
                        m_last.enviadoEn,
                        m_last.enviadoPor,
                        (CASE WHEN l.leidoEn IS NOT NULL AND l.leidoEn >= m_last.enviadoEn THEN true ELSE false END)
                    )
                FROM Hilo h
                LEFT JOIN h.mensajes m
                JOIN h.mensajes m_last
                LEFT JOIN LecturaHilo l ON l.hilo = h AND l.usuario.id = :usuarioId
                WHERE h.atleta.id = :atletaId
                AND h.contexto = :contexto
                AND m_last.enviadoEn = (SELECT MAX(m2.enviadoEn) FROM Mensaje m2 WHERE m2.hilo = h)
                GROUP BY h.id, h.titulo, h.categoria, h.contexto, h.creadoPor, h.creadoEn, m_last.texto, m_last.enviadoEn, m_last.enviadoPor, l.leidoEn
                ORDER BY m_last.enviadoEn DESC
            """)
    List<HiloResumenDTO> findResumenByAtletaAndContexto(
            @Param("atletaId") UUID atletaId,
            @Param("contexto") ContextoHilo contexto,
            @Param("usuarioId") UUID usuarioId
    );

    @Query("""
                SELECT new grit.sistema.backend.dto.communication.HiloResumenDTO(
                        h.id,
                        h.titulo,
                        h.categoria,
                        h.contexto,
                        h.creadoPor,
                        h.creadoEn,
                        CAST(COUNT(m) AS integer),
                        m_last.texto,
                        m_last.enviadoEn,
                        m_last.enviadoPor,
                        (CASE WHEN l.leidoEn IS NOT NULL AND l.leidoEn >= m_last.enviadoEn THEN true ELSE false END)
                    )
                FROM Hilo h
                JOIN h.atleta a
                JOIN Asignacion asig ON asig.atleta = a
                LEFT JOIN h.mensajes m
                JOIN h.mensajes m_last
                LEFT JOIN LecturaHilo l ON l.hilo = h AND l.usuario.id = :entrenadorId
                WHERE a.id = :atletaId
                AND asig.entrenador.id = :entrenadorId
                AND asig.activa = true
                AND h.contexto = :contexto
                AND m_last.enviadoEn = (SELECT MAX(m2.enviadoEn) FROM Mensaje m2 WHERE m2.hilo = h)
                GROUP BY h.id, h.titulo, h.categoria, h.contexto, h.creadoPor, h.creadoEn, m_last.texto, m_last.enviadoEn, m_last.enviadoPor, l.leidoEn
                ORDER BY m_last.enviadoEn DESC
            """)
    List<HiloResumenDTO> findResumenByAtletaForEntrenador(
            @Param("atletaId") UUID atletaId,
            @Param("entrenadorId") UUID entrenadorId,
            @Param("contexto") ContextoHilo contexto
    );
}
