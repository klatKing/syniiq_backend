package com.syniiq.syniiq_backend.dto;

import com.syniiq.syniiq_backend.model.Employee;
import com.syniiq.syniiq_backend.model.Project;
import com.syniiq.syniiq_backend.model.ServiceItem;
import com.syniiq.syniiq_backend.model.Testimonial;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

/** Formats de réponse de l'API : toujours { success, message, <données> }. */
public final class ApiModels {

    private ApiModels() {
    }

    // ---------- Réponses génériques ----------

    @Schema(description = "Réponse sans donnée : confirmation d'une suppression")
    public record MessageResponse(
            @Schema(description = "true si l'opération a réussi", example = "true") boolean success,
            @Schema(description = "Message décrivant le résultat", example = "Élément supprimé avec succès") String message
    ) {
    }

    @Schema(description = "Réponse d'erreur (codes 400, 401, 403, 404, 413)")
    public record ErrorResponse(
            @Schema(description = "Toujours false en cas d'erreur", example = "false") boolean success,
            @Schema(description = "Message d'erreur lisible", example = "Service introuvable : 42") String message,
            @Schema(description = "Erreurs de validation par champ (objet vide pour les autres erreurs)",
                    example = "{\"title\":\"Le titre est obligatoire\"}") Map<String, String> errors
    ) {
        public static ErrorResponse of(String message) {
            return new ErrorResponse(false, message, Map.of());
        }
    }

    // ---------- Authentification ----------

    @Schema(description = "Réponse contenant un utilisateur")
    public record UserResponse(
            @Schema(example = "true") boolean success,
            @Schema(example = "Connexion réussie") String message,
            @Schema(description = "Compte de l'utilisateur") UserDto user
    ) {
    }

    // ---------- Fichiers ----------

    @Schema(description = "Réponse après l'envoi d'une image")
    public record FileUploadResponse(
            @Schema(example = "true") boolean success,
            @Schema(example = "Image envoyée avec succès") String message,
            @Schema(description = "URL relative de l'image, à enregistrer dans photoUrl, imageUrl ou icon",
                    example = "/uploads/3f2a9c1e-7b1d-4c55-9a55-0a7d3a1c9e10.jpg") String url
    ) {
    }

    // ---------- Services ----------

    @Schema(description = "Réponse contenant un service")
    public record ServiceResponse(
            @Schema(example = "true") boolean success,
            @Schema(example = "Service récupéré avec succès") String message,
            @Schema(description = "Le service") ServiceItem service
    ) {
    }

    @Schema(description = "Réponse contenant une liste de services")
    public record ServiceListResponse(
            @Schema(example = "true") boolean success,
            @Schema(example = "Liste des services récupérée avec succès") String message,
            @Schema(description = "Les services") List<ServiceItem> services
    ) {
    }

    // ---------- Projets ----------

    @Schema(description = "Réponse contenant un projet")
    public record ProjectResponse(
            @Schema(example = "true") boolean success,
            @Schema(example = "Projet récupéré avec succès") String message,
            @Schema(description = "Le projet") Project project
    ) {
    }

    @Schema(description = "Réponse contenant une liste de projets")
    public record ProjectListResponse(
            @Schema(example = "true") boolean success,
            @Schema(example = "Liste des projets récupérée avec succès") String message,
            @Schema(description = "Les projets") List<Project> projects
    ) {
    }

    // ---------- Employés ----------

    @Schema(description = "Réponse contenant un employé")
    public record EmployeeResponse(
            @Schema(example = "true") boolean success,
            @Schema(example = "Employé récupéré avec succès") String message,
            @Schema(description = "L'employé") Employee employee
    ) {
    }

    @Schema(description = "Réponse contenant une liste d'employés")
    public record EmployeeListResponse(
            @Schema(example = "true") boolean success,
            @Schema(example = "Liste des employés récupérée avec succès") String message,
            @Schema(description = "Les employés") List<Employee> employees
    ) {
    }

    // ---------- Avis ----------

    @Schema(description = "Réponse contenant un avis client")
    public record TestimonialResponse(
            @Schema(example = "true") boolean success,
            @Schema(example = "Avis récupéré avec succès") String message,
            @Schema(description = "L'avis") Testimonial testimonial
    ) {
    }

    @Schema(description = "Réponse contenant une liste d'avis clients")
    public record TestimonialListResponse(
            @Schema(example = "true") boolean success,
            @Schema(example = "Liste des avis récupérée avec succès") String message,
            @Schema(description = "Les avis") List<Testimonial> testimonials
    ) {
    }

        @Schema(description = "Réponse de connexion : token JWT et compte connecté")
    public record LoginResponse(
            @Schema(example = "true") boolean success,
            @Schema(example = "Connexion réussie") String message,
            @Schema(description = "Token JWT à envoyer dans l'en-tête « Authorization: Bearer <token> »",
                    example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBzeW5paXEuY29tIn0.xxxxx") String token,
            @Schema(description = "Type du token", example = "Bearer") String tokenType,
            @Schema(description = "Durée de validité du token en secondes", example = "7200") long expiresIn,
            @Schema(description = "Compte connecté") UserDto user
    ) {
    }
}