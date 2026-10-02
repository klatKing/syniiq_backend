package com.syniiq.syniiq_backend.controller;

import com.syniiq.syniiq_backend.dto.ApiModels.*;
import com.syniiq.syniiq_backend.exception.ResourceNotFoundException;
import com.syniiq.syniiq_backend.model.Testimonial;
import com.syniiq.syniiq_backend.repository.TestimonialRepository;
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

@Tag(name = "Avis clients",
        description = "Avis des clients (nom, entreprise éventuelle, description, photo, note de 1 à 5 étoiles). "
                + "Lecture publique, écriture réservée à l'administrateur. "
                + "Changements diffusés en temps réel sur /topic/testimonials.")
@RestController
@RequestMapping("/api/testimonials")
@RequiredArgsConstructor
public class TestimonialController {

    private static final String TOPIC = "testimonials";

    private final TestimonialRepository repository;
    private final RealtimeService realtime;

    @Operation(summary = "Lister tous les avis",
            description = "Renvoie tous les avis clients, pour la section « Témoignages » du site. Endpoint public.")
    @ApiResponse(responseCode = "200", description = "Liste récupérée",
            content = @Content(schema = @Schema(implementation = TestimonialListResponse.class)))
    @GetMapping
    public TestimonialListResponse getAll() {
        return new TestimonialListResponse(true, "Liste des avis récupérée avec succès", repository.findAll());
    }

    @Operation(summary = "Récupérer un avis par son identifiant",
            description = "Renvoie le détail d'un avis client. Endpoint public.")
    @ApiResponse(responseCode = "200", description = "Avis trouvé",
            content = @Content(schema = @Schema(implementation = TestimonialResponse.class)))
    @ApiResponse(responseCode = "404", description = "Aucun avis avec cet identifiant",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{id}")
    public TestimonialResponse getById(@Parameter(description = "Identifiant de l'avis", example = "1")
                                       @PathVariable("id") Long id) {
        return new TestimonialResponse(true, "Avis récupéré avec succès", find(id));
    }

    @Operation(summary = "Ajouter un avis",
            description = "Ajoute un avis client. Nom, description et note (entier de 1 à 5) sont obligatoires ; "
                    + "l'entreprise et la photo sont optionnelles. Un message CREATED est diffusé sur "
                    + "/topic/testimonials. Réservé à l'administrateur.")
    @SecurityRequirement(name = "basicAuth")
    @ApiResponse(responseCode = "201", description = "Avis créé",
            content = @Content(schema = @Schema(implementation = TestimonialResponse.class)))
    @ApiResponse(responseCode = "400", description = "Champ obligatoire manquant, trop long, ou note hors de 1 à 5 (détail dans errors)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Non authentifié",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Droits administrateur requis",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TestimonialResponse create(@Valid @RequestBody Testimonial body) {
        body.setId(null);
        Testimonial saved = repository.save(body);
        realtime.publish(TOPIC, "CREATED", saved);
        return new TestimonialResponse(true, "Avis créé avec succès", saved);
    }

    @Operation(summary = "Modifier un avis",
            description = "Remplace tous les champs de l'avis. Les champs obligatoires doivent être renvoyés ; "
                    + "l'entreprise et la photo omises sont effacées. Un message UPDATED est diffusé sur "
                    + "/topic/testimonials. Réservé à l'administrateur.")
    @SecurityRequirement(name = "basicAuth")
    @ApiResponse(responseCode = "200", description = "Avis modifié",
            content = @Content(schema = @Schema(implementation = TestimonialResponse.class)))
    @ApiResponse(responseCode = "400", description = "Champ obligatoire manquant, trop long, ou note hors de 1 à 5 (détail dans errors)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Non authentifié",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Droits administrateur requis",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Aucun avis avec cet identifiant",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PutMapping("/{id}")
    public TestimonialResponse update(@Parameter(description = "Identifiant de l'avis à modifier", example = "1")
                                      @PathVariable("id") Long id,
                                      @Valid @RequestBody Testimonial body) {
        Testimonial existing = find(id);
        existing.setName(body.getName());
        existing.setCompany(body.getCompany());
        existing.setDescription(body.getDescription());
        existing.setPhotoUrl(body.getPhotoUrl());
        existing.setRating(body.getRating());
        Testimonial saved = repository.save(existing);
        realtime.publish(TOPIC, "UPDATED", saved);
        return new TestimonialResponse(true, "Avis modifié avec succès", saved);
    }

    @Operation(summary = "Supprimer un avis",
            description = "Supprime définitivement l'avis. Un message DELETED contenant l'identifiant est "
                    + "diffusé sur /topic/testimonials. Réservé à l'administrateur.")
    @SecurityRequirement(name = "basicAuth")
    @ApiResponse(responseCode = "200", description = "Avis supprimé",
            content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    @ApiResponse(responseCode = "401", description = "Non authentifié",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Droits administrateur requis",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Aucun avis avec cet identifiant",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @DeleteMapping("/{id}")
    public MessageResponse delete(@Parameter(description = "Identifiant de l'avis à supprimer", example = "1")
                                  @PathVariable("id") Long id) {
        repository.delete(find(id));
        realtime.publish(TOPIC, "DELETED", id);
        return new MessageResponse(true, "Avis supprimé avec succès");
    }

    private Testimonial find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Avis introuvable : " + id));
    }
}