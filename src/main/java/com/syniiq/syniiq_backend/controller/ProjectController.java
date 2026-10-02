package com.syniiq.syniiq_backend.controller;

import com.syniiq.syniiq_backend.dto.ApiModels.*;
import com.syniiq.syniiq_backend.exception.ResourceNotFoundException;
import com.syniiq.syniiq_backend.model.Project;
import com.syniiq.syniiq_backend.repository.ProjectRepository;
import com.syniiq.syniiq_backend.websocket.RealtimeService;
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

@Tag(name = "Projets",
        description = "Projets réalisés par l'entreprise (titre, description, image, catégorie, client, "
                + "technologies, lien). Lecture publique, écriture réservée à l'administrateur. "
                + "Changements diffusés en temps réel sur /topic/projects.")
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private static final String TOPIC = "projects";

    private final ProjectRepository repository;
    private final RealtimeService realtime;

    @Operation(summary = "Lister tous les projets",
            description = "Renvoie tous les projets réalisés, pour la section « Projets » du site. Endpoint public.")
    @ApiResponse(responseCode = "200", description = "Liste récupérée",
            content = @Content(schema = @Schema(implementation = ProjectListResponse.class)))
    @GetMapping
    public ProjectListResponse getAll() {
        return new ProjectListResponse(true, "Liste des projets récupérée avec succès", repository.findAll());
    }

    @Operation(summary = "Récupérer un projet par son identifiant",
            description = "Renvoie le détail d'un projet, par exemple pour une page de détail. Endpoint public.")
    @ApiResponse(responseCode = "200", description = "Projet trouvé",
            content = @Content(schema = @Schema(implementation = ProjectResponse.class)))
    @ApiResponse(responseCode = "404", description = "Aucun projet avec cet identifiant",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{id}")
    public ProjectResponse getById(@Parameter(description = "Identifiant du projet", example = "1")
                                   @PathVariable("id") Long id) {
        return new ProjectResponse(true, "Projet récupéré avec succès", find(id));
    }

    @Operation(summary = "Créer un projet",
            description = "Ajoute un projet. Titre, description et image sont obligatoires ; catégorie, client, "
                    + "technologies et lien sont optionnels. L'image doit d'abord être envoyée via "
                    + "POST /api/files/upload, puis son URL est placée dans imageUrl. "
                    + "Un message CREATED est diffusé sur /topic/projects. Réservé à l'administrateur.")
    @SecurityRequirement(name = "basicAuth")
    @ApiResponse(responseCode = "201", description = "Projet créé",
            content = @Content(schema = @Schema(implementation = ProjectResponse.class)))
    @ApiResponse(responseCode = "400", description = "Champ obligatoire manquant ou trop long (détail dans errors)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Non authentifié",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Droits administrateur requis",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(@Valid @RequestBody Project body) {
        body.setId(null);
        Project saved = repository.save(body);
        realtime.publish(TOPIC, "CREATED", saved);
        return new ProjectResponse(true, "Projet créé avec succès", saved);
    }

    @Operation(summary = "Modifier un projet",
            description = "Remplace tous les champs du projet. Les champs obligatoires doivent être renvoyés ; "
                    + "les champs optionnels omis sont effacés. Un message UPDATED est diffusé sur /topic/projects. "
                    + "Réservé à l'administrateur.")
    @SecurityRequirement(name = "basicAuth")
    @ApiResponse(responseCode = "200", description = "Projet modifié",
            content = @Content(schema = @Schema(implementation = ProjectResponse.class)))
    @ApiResponse(responseCode = "400", description = "Champ obligatoire manquant ou trop long (détail dans errors)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Non authentifié",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Droits administrateur requis",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Aucun projet avec cet identifiant",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PutMapping("/{id}")
    public ProjectResponse update(@Parameter(description = "Identifiant du projet à modifier", example = "1")
                                  @PathVariable("id") Long id,
                                  @Valid @RequestBody Project body) {
        Project existing = find(id);
        existing.setTitle(body.getTitle());
        existing.setDescription(body.getDescription());
        existing.setImageUrl(body.getImageUrl());
        existing.setCategory(body.getCategory());
        existing.setClientName(body.getClientName());
        existing.setTechnologies(body.getTechnologies());
        existing.setProjectUrl(body.getProjectUrl());
        Project saved = repository.save(existing);
        realtime.publish(TOPIC, "UPDATED", saved);
        return new ProjectResponse(true, "Projet modifié avec succès", saved);
    }

    @Operation(summary = "Supprimer un projet",
            description = "Supprime définitivement le projet (l'image reste sur le disque). Un message DELETED "
                    + "contenant l'identifiant est diffusé sur /topic/projects. Réservé à l'administrateur.")
    @SecurityRequirement(name = "basicAuth")
    @ApiResponse(responseCode = "200", description = "Projet supprimé",
            content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    @ApiResponse(responseCode = "401", description = "Non authentifié",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Droits administrateur requis",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Aucun projet avec cet identifiant",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @DeleteMapping("/{id}")
    public MessageResponse delete(@Parameter(description = "Identifiant du projet à supprimer", example = "1")
                                  @PathVariable("id") Long id) {
        repository.delete(find(id));
        realtime.publish(TOPIC, "DELETED", id);
        return new MessageResponse(true, "Projet supprimé avec succès");
    }

    private Project find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projet introuvable : " + id));
    }
}