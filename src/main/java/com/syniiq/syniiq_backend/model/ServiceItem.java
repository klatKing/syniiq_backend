package com.syniiq.syniiq_backend.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Un service proposé par l'entreprise, affiché dans la section « Services » du site")
@Entity
@Table(name = "services")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceItem {

    @Schema(description = "Identifiant unique, généré automatiquement (ignoré à la création et à la modification)",
            example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "Icône du service : nom d'une icône (ex: fa-code) ou URL d'une image obtenue via /api/files/upload",
            example = "fa-code", maxLength = 255)
    @NotBlank(message = "L'icône est obligatoire")
    @Size(max = 255)
    @Column(nullable = false)
    private String icon;

    @Schema(description = "Titre du service", example = "Développement web", maxLength = 150)
    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String title;

    @Schema(description = "Description détaillée du service",
            example = "Conception de sites vitrines, e-commerce et applications web sur mesure.", maxLength = 2000)
    @NotBlank(message = "La description est obligatoire")
    @Size(max = 2000)
    @Column(nullable = false, length = 2000)
    private String description;
}