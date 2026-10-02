package com.syniiq.syniiq_backend.controller;

import com.syniiq.syniiq_backend.dto.ApiModels.*;
import com.syniiq.syniiq_backend.exception.ResourceNotFoundException;
import com.syniiq.syniiq_backend.model.Employee;
import com.syniiq.syniiq_backend.repository.EmployeeRepository;
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

@Tag(name = "Employés",
        description = "Membres de l'équipe (photo, nom, poste). Lecture publique, écriture réservée à "
                + "l'administrateur. Changements diffusés en temps réel sur /topic/employees.")
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private static final String TOPIC = "employees";

    private final EmployeeRepository repository;
    private final RealtimeService realtime;

    @Operation(summary = "Lister tous les employés",
            description = "Renvoie tous les membres de l'équipe, pour la section « Équipe » du site. Endpoint public.")
    @ApiResponse(responseCode = "200", description = "Liste récupérée",
            content = @Content(schema = @Schema(implementation = EmployeeListResponse.class)))
    @GetMapping
    public EmployeeListResponse getAll() {
        return new EmployeeListResponse(true, "Liste des employés récupérée avec succès", repository.findAll());
    }

    @Operation(summary = "Récupérer un employé par son identifiant",
            description = "Renvoie la fiche d'un employé (nom, poste, photo). Endpoint public.")
    @ApiResponse(responseCode = "200", description = "Employé trouvé",
            content = @Content(schema = @Schema(implementation = EmployeeResponse.class)))
    @ApiResponse(responseCode = "404", description = "Aucun employé avec cet identifiant",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{id}")
    public EmployeeResponse getById(@Parameter(description = "Identifiant de l'employé", example = "1")
                                    @PathVariable("id") Long id) {
        return new EmployeeResponse(true, "Employé récupéré avec succès", find(id));
    }

    @Operation(summary = "Ajouter un employé",
            description = "Ajoute un membre à l'équipe. Nom, poste et photo sont obligatoires. La photo doit "
                    + "d'abord être envoyée via POST /api/files/upload, puis son URL est placée dans photoUrl. "
                    + "Un message CREATED est diffusé sur /topic/employees. Réservé à l'administrateur.")
    @SecurityRequirement(name = "basicAuth")
    @ApiResponse(responseCode = "201", description = "Employé créé",
            content = @Content(schema = @Schema(implementation = EmployeeResponse.class)))
    @ApiResponse(responseCode = "400", description = "Champ obligatoire manquant ou trop long (détail dans errors)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Non authentifié",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Droits administrateur requis",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse create(@Valid @RequestBody Employee body) {
        body.setId(null);
        Employee saved = repository.save(body);
        realtime.publish(TOPIC, "CREATED", saved);
        return new EmployeeResponse(true, "Employé créé avec succès", saved);
    }

    @Operation(summary = "Modifier un employé",
            description = "Remplace le nom, le poste et la photo de l'employé. Tous les champs doivent être "
                    + "renvoyés. Un message UPDATED est diffusé sur /topic/employees. Réservé à l'administrateur.")
    @SecurityRequirement(name = "basicAuth")
    @ApiResponse(responseCode = "200", description = "Employé modifié",
            content = @Content(schema = @Schema(implementation = EmployeeResponse.class)))
    @ApiResponse(responseCode = "400", description = "Champ obligatoire manquant ou trop long (détail dans errors)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Non authentifié",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Droits administrateur requis",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Aucun employé avec cet identifiant",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PutMapping("/{id}")
    public EmployeeResponse update(@Parameter(description = "Identifiant de l'employé à modifier", example = "1")
                                   @PathVariable("id") Long id,
                                   @Valid @RequestBody Employee body) {
        Employee existing = find(id);
        existing.setName(body.getName());
        existing.setPosition(body.getPosition());
        existing.setPhotoUrl(body.getPhotoUrl());
        Employee saved = repository.save(existing);
        realtime.publish(TOPIC, "UPDATED", saved);
        return new EmployeeResponse(true, "Employé modifié avec succès", saved);
    }

    @Operation(summary = "Supprimer un employé",
            description = "Supprime définitivement l'employé (sa photo reste sur le disque). Un message DELETED "
                    + "contenant l'identifiant est diffusé sur /topic/employees. Réservé à l'administrateur.")
    @SecurityRequirement(name = "basicAuth")
    @ApiResponse(responseCode = "200", description = "Employé supprimé",
            content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    @ApiResponse(responseCode = "401", description = "Non authentifié",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Droits administrateur requis",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Aucun employé avec cet identifiant",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @DeleteMapping("/{id}")
    public MessageResponse delete(@Parameter(description = "Identifiant de l'employé à supprimer", example = "1")
                                  @PathVariable("id") Long id) {
        repository.delete(find(id));
        realtime.publish(TOPIC, "DELETED", id);
        return new MessageResponse(true, "Employé supprimé avec succès");
    }

    private Employee find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employé introuvable : " + id));
    }
}