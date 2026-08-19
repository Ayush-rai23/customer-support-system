package com.support.backend.repository;

import com.support.backend.dto.StatusCount;
import com.support.backend.entity.Ticket;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketRepository
        extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

    /** All-time backlog snapshot, independent of any date range. */
    @Query("SELECT new com.support.backend.dto.StatusCount(t.status, COUNT(t)) FROM Ticket t GROUP BY t.status")
    List<StatusCount> countByStatus();

    /** Daily ticket counts per category (null category -> "Uncategorized"), for the trend charts. */
    @Query(value = """
            SELECT DATE_TRUNC('day', t.created_at) AS day,
                   COALESCE(c.name, 'Uncategorized') AS categoryName,
                   COUNT(*) AS cnt
            FROM tickets t
            LEFT JOIN categories c ON c.id = t.category_id
            WHERE t.created_at >= :since
            GROUP BY DATE_TRUNC('day', t.created_at), COALESCE(c.name, 'Uncategorized')
            ORDER BY day
            """, nativeQuery = true)
    List<DailyCategoryCountRow> dailyCategoryCounts(@Param("since") Instant since);

    /** Average minutes from ticket creation to its first outbound (AI or admin) reply. Null if none yet. */
    @Query(value = """
            SELECT AVG(EXTRACT(EPOCH FROM (fr.first_outbound - t.created_at)) / 60.0)::double precision
            FROM tickets t
            JOIN (
                SELECT ticket_id, MIN(created_at) AS first_outbound
                FROM ticket_messages
                WHERE direction = 'OUTBOUND'
                GROUP BY ticket_id
            ) fr ON fr.ticket_id = t.id
            WHERE t.created_at >= :since
            """, nativeQuery = true)
    Double averageResponseMinutes(@Param("since") Instant since);
}
