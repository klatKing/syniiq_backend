package com.syniiq.syniiq_backend.repository;

import com.syniiq.syniiq_backend.model.AnalyticsEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, Long> {

    long countByEventType(String eventType);

    @Query("select count(distinct a.userSessionId) from AnalyticsEvent a where a.eventType = :eventType")
    long countDistinctSessions(@Param("eventType") String eventType);

    // [0] = date (java.sql.Date), [1] = nombre de vues ce jour-là
    @Query(value = """
            select date(created_at) as day, count(*) as visits
            from analytics_events
            where event_type = :eventType and created_at >= :since
            group by day
            order by day
            """, nativeQuery = true)
    List<Object[]> findDailyVisits(@Param("eventType") String eventType, @Param("since") Instant since);

    // [0] = page_path, [1] = nombre de vues
    @Query(value = """
            select page_path, count(*) as views
            from analytics_events
            where event_type = :eventType and created_at >= :since
            group by page_path
            order by views desc
            """, nativeQuery = true)
    List<Object[]> findTopPages(@Param("eventType") String eventType, @Param("since") Instant since);
}