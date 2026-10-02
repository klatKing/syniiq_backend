package com.syniiq.syniiq_backend.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Un avis client, affiché dans la section « Témoignages » du site")
@Entity
@Table(name = "testimonials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Testimonial {

    @Schema(description = "Identifiant unique, généré automatiquement (ignoré à la création et à la modification)",
            example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "Nom de la personne qui donne l'avis", example = "Marie Kamga", maxLength = 150)
    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String name;

    @Schema(description = "Entreprise de la personne (optionnel : tous les clients n'en ont pas)",
            example = "Acme SARL", maxLength = 150, nullable = true)
    @Size(max = 150)
    @Column(length = 150)
    private String company;

    @Schema(description = "Texte de l'avis",
            example = "Une équipe professionnelle, le site a été livré dans les délais.", maxLength = 2000)
    @NotBlank(message = "La description est obligatoire")
    @Size(max = 2000)
    @Column(nullable = false, length = 2000)
    private String description;

    @Schema(description = "URL de la photo, obtenue via /api/files/upload (optionnel)",
            example = "/uploads/3f2a9c1e-7b1d-4c55-9a55-0a7d3a1c9e10.jpg", maxLength = 500, nullable = true)
    @Size(max = 500)
    @Column(length = 500)
    private String photoUrl;

    @Schema(description = "Note en nombre d'étoiles, de 1 à 5", example = "5", minimum = "1", maximum = "5")
    @Min(value = 1, message = "La note minimale est 1")
    @Max(value = 5, message = "La note maximale est 5")
    @Column(nullable = false)
    private int rating;
}