package com.syniiq.syniiq_backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Identifiants de connexion de l'administrateur")
public record LoginRequest(

        @Schema(description = "Adresse email du compte", example = "admin@syniiq.com")
        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Email invalide")
        String email,

        @Schema(description = "Mot de passe du compte", example = "ChangeMe123!", format = "password")
        @NotBlank(message = "Le mot de passe est obligatoire")
        String password
) {
}