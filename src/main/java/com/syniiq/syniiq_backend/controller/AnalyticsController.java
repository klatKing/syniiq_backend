package com.syniiq.syniiq_backend.controller;

import com.syniiq.syniiq_backend.dto.AnalyticsDto.*;
import com.syniiq.syniiq_backend.dto.ApiModels.ErrorResponse;
import com.syniiq.syniiq_backend.dto.ApiModels.MessageResponse;
import com.syniiq.syniiq_backend.model.AnalyticsEvent;
import com.syniiq.syniiq_backend.repository.AnalyticsEventRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDate;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

@Tag(name = "Statistiques", description = "Collecte et lecture des statistiques de fréquentation du site public.")
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private static final String PAGE_VIEW = "page_view";
    private static final DateTimeFormatter DAY_LABEL =
            DateTimeFormatter.ofPattern("dd MMM", Locale.FRENCH);

    private final AnalyticsEventRepository repository;

    @Operation(summary = "Enregistrer un événement",
            description = "Enregistre une vue de page (ou un autre événement) envoyée par le site public. "
                    + "Endpoint public, appelé automatiquement à chaque changement de page.")
    @ApiResponse(responseCode = "201", description = "Événement enregistré",
            content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    @ApiResponse(responseCode = "400", description = "Données invalides",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/track")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse track(@Valid @RequestBody TrackEventRequest request) {
        AnalyticsEvent event = new AnalyticsEvent();
        event.setPagePath(request.pagePath());
        event.setEventType(request.eventType());
        event.setUserSessionId(request.sessionId());
        repository.save(event);
        return new MessageResponse(true, "Événement enregistré");
    }

    @Operation(summary = "Résumé des statistiques",
            description = "Renvoie le nombre total de vues, le nombre de sessions distinctes, la fréquentation "
                    + "par jour et les pages les plus visitées, sur les `days` derniers jours. "
                    + "Réservé à l'administrateur.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Statistiques récupérées",
            content = @Content(schema = @Schema(implementation = AnalyticsSummaryResponse.class)))
    @ApiResponse(responseCode = "401", description = "Non authentifié",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Droits administrateur requis",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/summary")
    public AnalyticsSummaryResponse summary(
            @Parameter(description = "Nombre de jours à couvrir", example = "14")
            @RequestParam(name = "days", defaultValue = "14") int days) {

        Instant since = Instant.now().minus(days, ChronoUnit.DAYS);

        long totalViews = repository.countByEventType(PAGE_VIEW);
        long totalSessions = repository.countDistinctSessions(PAGE_VIEW);

        List<DailyVisit> dailyVisits = repository.findDailyVisits(PAGE_VIEW, since).stream()
                .map(row -> new DailyVisit(formatDay(row[0]), toLong(row[1])))
                .toList();

              List<TopPage> topPages = repository.findTopPages(PAGE_VIEW, since).stream()
                .limit(5)
                .map(row -> new TopPage(formatPagePath((String) row[0]), toLong(row[1])))
                .toList();

        return new AnalyticsSummaryResponse(
                true, "Statistiques récupérées avec succès", totalViews, totalSessions, dailyVisits, topPages);
    }

     private static String formatDay(Object rawDate) {
        LocalDate date;
        if (rawDate instanceof LocalDate d) {
            date = d;
        } else if (rawDate instanceof java.sql.Date d) {
            date = d.toLocalDate();
        } else if (rawDate instanceof Instant i) {
            date = i.atZone(ZoneId.systemDefault()).toLocalDate();
        } else {
            throw new IllegalStateException("Type de date inattendu : " + rawDate.getClass());
        }
        return date.format(DAY_LABEL);
    }
    private static String formatPagePath(String path) {
        if (path == null || path.isBlank() || path.equals("/")) return "Accueil";
        return path;
    }

    private static long toLong(Object count) {
        return ((Number) count).longValue();
    }
}