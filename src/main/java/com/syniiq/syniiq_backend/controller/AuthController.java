package com.syniiq.syniiq_backend.controller;

import com.syniiq.syniiq_backend.config.JwtService;
import com.syniiq.syniiq_backend.dto.ApiModels.ErrorResponse;
import com.syniiq.syniiq_backend.dto.ApiModels.LoginResponse;
import com.syniiq.syniiq_backend.dto.ApiModels.UserResponse;
import com.syniiq.syniiq_backend.dto.LoginRequest;
import com.syniiq.syniiq_backend.dto.UserDto;
import com.syniiq.syniiq_backend.exception.ResourceNotFoundException;
import com.syniiq.syniiq_backend.model.AppUser;
import com.syniiq.syniiq_backend.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Tag(name = "Authentification",
        description = "Connexion de l'administrateur par email et mot de passe. Le compte est créé "
                + "automatiquement au premier lancement à partir des propriétés app.admin.* de "
                + "application.properties. La connexion renvoie un token JWT qui donne accès aux endpoints admin.")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Operation(summary = "Se connecter et obtenir un token",
            description = "Vérifie l'email et le mot de passe. En cas de succès, renvoie un token JWT (champ `token`) "
                    + "et les informations du compte (champ `user`). Le token doit ensuite être envoyé dans "
                    + "l'en-tête `Authorization: Bearer <token>` pour créer, modifier ou supprimer des éléments "
                    + "et envoyer des images. Il expire après `expiresIn` secondes : l'administrateur doit alors "
                    + "se reconnecter. Endpoint public.")
    @ApiResponse(responseCode = "200", description = "Connexion réussie, token renvoyé",
            content = @Content(schema = @Schema(implementation = LoginResponse.class)))
    @ApiResponse(responseCode = "400", description = "Email ou mot de passe manquant, ou email mal formé",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Email ou mot de passe incorrect, ou compte désactivé",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        AppUser user = userRepository.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new BadCredentialsException("Email ou mot de passe incorrect"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Email ou mot de passe incorrect");
        }
        if (!user.isActif()) {
            throw new BadCredentialsException("Compte désactivé");
        }

        return new LoginResponse(true, "Connexion réussie",
                jwtService.generateToken(user), "Bearer", jwtService.getExpiresInSeconds(), UserDto.from(user));
    }

    @Operation(summary = "Récupérer le compte connecté",
            description = "Renvoie les informations du compte identifié par le token JWT. Utile pour vérifier, "
                    + "au chargement du back-office, que le token enregistré est toujours valide.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Compte récupéré",
            content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @ApiResponse(responseCode = "401", description = "Token absent, invalide ou expiré",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Compte sans droits administrateur",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/me")
    public UserResponse me(Principal principal) {
        AppUser user = userRepository.findByEmailIgnoreCase(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        return new UserResponse(true, "Compte récupéré avec succès", UserDto.from(user));
    }
}