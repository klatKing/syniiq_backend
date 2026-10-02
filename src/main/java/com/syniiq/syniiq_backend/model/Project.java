package com.syniiq.syniiq_backend.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Un projet réalisé par l'entreprise, affiché dans la section « Projets » du site")
@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    @Schema(description = "Identifiant unique, généré automatiquement (ignoré à la création et à la modification)",
            example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "Titre du projet", example = "Plateforme e-commerce Mbeka", maxLength = 150)
    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String title;

    @Schema(description = "Description du projet : contexte, objectifs et résultats",
            example = "Boutique en ligne avec paiement mobile et gestion des stocks.", maxLength = 3000)
    @NotBlank(message = "La description est obligatoire")
    @Size(max = 3000)
    @Column(nullable = false, length = 3000)
    private String description;

    @Schema(description = "URL de l'image du projet, obtenue via /api/files/upload",
            example = "/uploads/3f2a9c1e-7b1d-4c55-9a55-0a7d3a1c9e10.jpg", maxLength = 500)
    @NotBlank(message = "L'image est obligatoire")
    @Size(max = 500)
    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Schema(description = "Catégorie du projet (optionnel)", example = "Site web", maxLength = 100, nullable = true)
    @Size(max = 100)
    private String category;

    @Schema(description = "Nom du client (optionnel)", example = "Mbeka SARL", maxLength = 150, nullable = true)
    @Size(max = 150)
    private String clientName;

    @Schema(description = "Technologies utilisées, séparées par des virgules (optionnel)",
            example = "React, Spring Boot, MySQL", maxLength = 500, nullable = true)
    @Size(max = 500)
    @Column(length = 500)
    private String technologies;

    @Schema(description = "Lien vers le projet en ligne (optionnel)",
            example = "https://www.mbeka.com", maxLength = 500, nullable = true)
    @Size(max = 500)
    @Column(length = 500)
    private String projectUrl;
}