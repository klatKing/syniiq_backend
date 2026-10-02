package com.syniiq.syniiq_backend.controller;

import com.syniiq.syniiq_backend.dto.ApiModels.*;
import com.syniiq.syniiq_backend.exception.ResourceNotFoundException;
import com.syniiq.syniiq_backend.model.ServiceItem;
import com.syniiq.syniiq_backend.repository.ServiceItemRepository;
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

@Tag(name = "Services",
        description = "Services proposés par l'entreprise (icône, titre, description). "
                + "Lecture publique, écriture réservée à l'administrateur. "
                + "Changements diffusés en temps réel sur /topic/services.")
@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceItemController {

    private static final String TOPIC = "services";

    private final ServiceItemRepository repository;
    private final RealtimeService realtime;

    @Operation(summary = "Lister tous les services",
            description = "Renvoie tous les services de l'entreprise, pour la section « Services » du site. "
                    + "Endpoint public. La liste est vide si aucun service n'existe.")
    @ApiResponse(responseCode = "200", description = "Liste récupérée",
            content = @Content(schema = @Schema(implementation = ServiceListResponse.class)))
    @GetMapping
    public ServiceListResponse getAll() {
        return new ServiceListResponse(true, "Liste des services récupérée avec succès", repository.findAll());
    }

    @Operation(summary = "Récupérer un service par son identifiant",
            description = "Renvoie le service correspondant à l'identifiant. Endpoint public.")
    @ApiResponse(responseCode = "200", description = "Service trouvé",
            content = @Content(schema = @Schema(implementation = ServiceResponse.class)))
    @ApiResponse(responseCode = "404", description = "Aucun service avec cet identifiant",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{id}")
    public ServiceResponse getById(@Parameter(description = "Identifiant du service", example = "1")
                                   @PathVariable("id") Long id) {
        return new ServiceResponse(true, "Service récupéré avec succès", find(id));
    }

    @Operation(summary = "Créer un service",
            description = "Ajoute un nouveau service. Icône, titre et description sont obligatoires. "
                    + "L'identifiant est généré automatiquement (toute valeur envoyée est ignorée). "
                    + "Un message CREATED est diffusé sur /topic/services. Réservé à l'administrateur.")
    @SecurityRequirement(name = "basicAuth")
    @ApiResponse(responseCode = "201", description = "Service créé",
            content = @Content(schema = @Schema(implementation = ServiceResponse.class)))
    @ApiResponse(responseCode = "400", description = "Champ obligatoire manquant ou trop long (détail dans errors)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Non authentifié",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Droits administrateur requis",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceResponse create(@Valid @RequestBody ServiceItem body) {
        body.setId(null);
        ServiceItem saved = repository.save(body);
        realtime.publish(TOPIC, "CREATED", saved);
        return new ServiceResponse(true, "Service créé avec succès", saved);
    }

    @Operation(summary = "Modifier un service",
            description = "Remplace l'icône, le titre et la description du service. Tous les champs obligatoires "
                    + "doivent être renvoyés. Un message UPDATED est diffusé sur /topic/services. "
                    + "Réservé à l'administrateur.")
    @SecurityRequirement(name = "basicAuth")
    @ApiResponse(responseCode = "200", description = "Service modifié",
            content = @Content(schema = @Schema(implementation = ServiceResponse.class)))
    @ApiResponse(responseCode = "400", description = "Champ obligatoire manquant ou trop long (détail dans errors)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Non authentifié",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Droits administrateur requis",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Aucun service avec cet identifiant",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PutMapping("/{id}")
    public ServiceResponse update(@Parameter(description = "Identifiant du service à modifier", example = "1")
                                  @PathVariable("id") Long id,
                                  @Valid @RequestBody ServiceItem body) {
        ServiceItem existing = find(id);
        existing.setIcon(body.getIcon());
        existing.setTitle(body.getTitle());
        existing.setDescription(body.getDescription());
        ServiceItem saved = repository.save(existing);
        realtime.publish(TOPIC, "UPDATED", saved);
        return new ServiceResponse(true, "Service modifié avec succès", saved);
    }

    @Operation(summary = "Supprimer un service",
            description = "Supprime définitivement le service. Un message DELETED contenant l'identifiant est "
                    + "diffusé sur /topic/services. Réservé à l'administrateur.")
    @SecurityRequirement(name = "basicAuth")
    @ApiResponse(responseCode = "200", description = "Service supprimé",
            content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    @ApiResponse(responseCode = "401", description = "Non authentifié",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Droits administrateur requis",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Aucun service avec cet identifiant",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @DeleteMapping("/{id}")
    public MessageResponse delete(@Parameter(description = "Identifiant du service à supprimer", example = "1")
                                  @PathVariable("id") Long id) {
        repository.delete(find(id));
        realtime.publish(TOPIC, "DELETED", id);
        return new MessageResponse(true, "Service supprimé avec succès");
    }

    private ServiceItem find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service introuvable : " + id));
    }
}