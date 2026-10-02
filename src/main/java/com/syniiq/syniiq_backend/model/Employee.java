package com.syniiq.syniiq_backend.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Un membre de l'équipe, affiché dans la section « Équipe » du site")
@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Schema(description = "Identifiant unique, généré automatiquement (ignoré à la création et à la modification)",
            example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "Nom complet de l'employé", example = "Jean Dupont", maxLength = 150)
    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String name;

    @Schema(description = "Poste occupé dans l'entreprise", example = "Développeur Full Stack", maxLength = 150)
    @NotBlank(message = "Le poste est obligatoire")
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String position;

    @Schema(description = "URL de la photo, obtenue via /api/files/upload",
            example = "/uploads/3f2a9c1e-7b1d-4c55-9a55-0a7d3a1c9e10.jpg", maxLength = 500)
    @NotBlank(message = "La photo est obligatoire")
    @Size(max = 500)
    @Column(nullable = false, length = 500)
    private String photoUrl;
}