package com.syniiq.syniiq_backend.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.syniiq.syniiq_backend.dto.ApiModels.ErrorResponse;
import com.syniiq.syniiq_backend.dto.ApiModels.FileUploadResponse;
import com.syniiq.syniiq_backend.dto.ApiModels.MessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Tag(name = "Fichiers",
        description = "Envoi et suppression des images (photos d'employés, images de projets, icônes, photos d'avis). "
                + "Les fichiers sont stockés de façon permanente sur Cloudinary.")
@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final String FOLDER = "syniiq";

    private final Cloudinary cloudinary;

    public FileController(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Operation(summary = "Envoyer une image",
            description = "Envoie une image vers Cloudinary. Formats acceptés : jpg, jpeg, png, gif, webp. "
                    + "Taille maximale : 5 Mo. La réponse contient l'URL complète (https://res.cloudinary.com/...) "
                    + "à enregistrer dans photoUrl, imageUrl ou icon. Réservé à l'administrateur.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "201", description = "Image enregistrée",
            content = @Content(schema = @Schema(implementation = FileUploadResponse.class)))
    @ApiResponse(responseCode = "400", description = "Fichier vide ou format non autorisé",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Non authentifié",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Droits administrateur requis",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "413", description = "Fichier de plus de 5 Mo",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public FileUploadResponse upload(
            @Parameter(description = "Image à envoyer (champ multipart nommé « file »)")
            @RequestParam("file") MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est vide");
        }

        String original = StringUtils.cleanPath(Objects.requireNonNullElse(file.getOriginalFilename(), ""));
        String extension = StringUtils.getFilenameExtension(original);
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("Format non autorisé. Formats acceptés : " + ALLOWED_EXTENSIONS);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Le fichier n'est pas une image");
        }

        Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", FOLDER,
                "public_id", UUID.randomUUID().toString(),
                "resource_type", "image",
                "overwrite", false));

        String url = (String) result.get("secure_url");
        log.info("Image envoyée sur Cloudinary : {}", url);

        return new FileUploadResponse(true, "Image envoyée avec succès", url);
    }

    @Operation(summary = "Supprimer une image",
            description = "Supprime une image de Cloudinary à partir de son URL complète "
                    + "(ex: https://res.cloudinary.com/.../syniiq/3f2a9c1e.jpg). "
                    + "Ne renvoie pas d'erreur si l'image n'existe déjà plus. Réservé à l'administrateur.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Image supprimée (ou déjà absente)",
            content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    @ApiResponse(responseCode = "400", description = "URL invalide",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Non authentifié",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Droits administrateur requis",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @DeleteMapping
    public MessageResponse delete(
            @Parameter(description = "URL complète de l'image à supprimer")
            @RequestParam("url") String url) throws IOException {

        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL invalide");
        }

        // Anciennes images locales : il n'y a plus rien à supprimer.
        if (url.startsWith("/uploads/")) {
            return new MessageResponse(true, "Image déjà absente");
        }

        String publicId = extractPublicId(url);
        Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
        boolean deleted = "ok".equals(result.get("result"));
        log.info(deleted ? "Image supprimée : {}" : "Image déjà absente : {}", publicId);

        return new MessageResponse(true, deleted ? "Image supprimée" : "Image déjà absente");
    }

    /** Extrait l'identifiant public (ex: "syniiq/3f2a9c1e") d'une URL Cloudinary de notre compte. */
    private String extractPublicId(String url) {
        String cloudName = cloudinary.config.cloudName;
        String prefix = "https://res.cloudinary.com/" + cloudName + "/image/upload/";
        if (!url.startsWith(prefix)) {
            throw new IllegalArgumentException("URL invalide : seules les images de ce compte Cloudinary peuvent être supprimées");
        }
        String rest = url.substring(prefix.length());
        rest = rest.replaceFirst("^v\\d+/", "");
        int dot = rest.lastIndexOf('.');
        if (dot > 0) {
            rest = rest.substring(0, dot);
        }
        if (!rest.startsWith(FOLDER + "/")) {
            throw new IllegalArgumentException("URL invalide");
        }
        return rest;
    }
}