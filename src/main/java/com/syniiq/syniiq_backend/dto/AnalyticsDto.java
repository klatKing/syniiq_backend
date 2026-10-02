package com.syniiq.syniiq_backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class AnalyticsDto {

    private AnalyticsDto() {}

    @Schema(description = "Événement envoyé par le site public à chaque changement de page")
    public record TrackEventRequest(
            @Schema(description = "Chemin de la page visitée", example = "/services")
            @NotBlank(message = "pagePath est obligatoire")
            @Size(max = 255)
            String pagePath,

            @Schema(description = "Type d'événement", example = "page_view")
            @NotBlank(message = "eventType est obligatoire")
            @Size(max = 50)
            String eventType,

            @Schema(description = "Identifiant unique généré par le navigateur pour la session en cours",
                    example = "b3f1c2a4-...")
            @NotBlank(message = "sessionId est obligatoire")
            @Size(max = 100)
            String sessionId
    ) {
    }

    @Schema(description = "Une entrée du graphique de fréquentation")
    public record DailyVisit(
            @Schema(example = "24 sept.") String date,
            @Schema(example = "120") long visits
    ) {
    }

    @Schema(description = "Une page classée parmi les plus visitées")
    public record TopPage(
            @Schema(example = "/services") String path,
            @Schema(example = "84") long views
    ) {
    }

    @Schema(description = "Résumé des statistiques de fréquentation du site public")
    public record AnalyticsSummaryResponse(
            @Schema(example = "true") boolean success,
            @Schema(example = "Statistiques récupérées avec succès") String message,
            @Schema(description = "Nombre total de vues de pages sur la période") long totalViews,
            @Schema(description = "Nombre de sessions distinctes sur la période") long totalSessions,
            @Schema(description = "Vues par jour, dans l'ordre chronologique") List<DailyVisit> dailyVisits,
            @Schema(description = "Les 5 pages les plus visitées, triées par nombre de vues") List<TopPage> topPages
    ) {
    }
}