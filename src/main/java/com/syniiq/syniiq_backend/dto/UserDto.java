package com.syniiq.syniiq_backend.dto;

import com.syniiq.syniiq_backend.model.AppUser;
import com.syniiq.syniiq_backend.model.RoleSysteme;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Informations d'un compte utilisateur. Le mot de passe n'est jamais renvoyé.")
public record UserDto(

        @Schema(description = "Identifiant unique du compte", example = "1")
        Long id,

        @Schema(description = "Nom complet de l'utilisateur", example = "Administrateur Syniiq")
        String nom,

        @Schema(description = "Numéro de téléphone", example = "+237600000000")
        String telephone,

        @Schema(description = "Adresse email (sert d'identifiant de connexion)", example = "admin@syniiq.com")
        String email,

        @Schema(description = "Rôle système : ROLE_ADMIN peut tout modifier, ROLE_CLIENT est en lecture seule", example = "ROLE_ADMIN")
        RoleSysteme roleSysteme,

        @Schema(description = "URL de la photo de profil (peut être null)", example = "/uploads/3f2a9c1e.jpg", nullable = true)
        String photoProfil,

        @Schema(description = "true si le compte est actif, false s'il est désactivé (connexion impossible)", example = "true")
        boolean actif,

        @Schema(description = "Date de création du compte (UTC, format ISO 8601)", example = "2026-09-23T18:45:14.259Z")
        Instant createdAt
) {
    public static UserDto from(AppUser u) {
        return new UserDto(u.getId(), u.getNom(), u.getTelephone(), u.getEmail(),
                u.getRoleSysteme(), u.getPhotoProfil(), u.isActif(), u.getCreatedAt());
    }
}