package com.syniiq.syniiq_backend.controller;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Tag(name = "Fichiers",
        description = "Envoi et suppression des images (photos d'employés, images de projets, icônes, photos d'avis). "
                + "Les fichiers sont enregistrés sur le serveur et servis publiquement sous /uploads/.")
@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final Path uploadDir;

    public FileController(@Value("${app.upload-dir}") String dir) throws IOException {
        this.uploadDir = Paths.get(dir).toAbsolutePath().normalize();
        Files.createDirectories(uploadDir);
    }

    @Operation(summary = "Envoyer une image",
            description = "Enregistre une image sur le serveur sous un nom unique généré automatiquement. "
                    + "Formats acceptés : jpg, jpeg, png, gif, webp. Taille maximale : 5 Mo. "
                    + "La réponse contient l'URL relative de l'image (ex: /uploads/xxxx.jpg) à enregistrer dans "
                    + "photoUrl, imageUrl ou icon ; l'image est ensuite visible à l'adresse "
                    + "http://localhost:8080 + url. Réservé à l'administrateur.")
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
    @PostMapping(value = "/upload", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
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

        String fileName = UUID.randomUUID() + "." + extension.toLowerCase();
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, uploadDir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        }

        return new FileUploadResponse(true, "Image envoyée avec succès", "/uploads/" + fileName);
    }

    @Operation(summary = "Supprimer une image",
            description = "Supprime un fichier du dossier d'upload à partir de son URL relative "
                    + "(ex: /uploads/3f2a9c1e.jpg), typiquement l'ancienne image remplacée lors d'une modification. "
                    + "Ne renvoie jamais d'erreur si le fichier n'existe déjà plus. Réservé à l'administrateur.")
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
            @Parameter(description = "URL relative de l'image à supprimer", example = "/uploads/3f2a9c1e.jpg")
            @RequestParam("url") String url) throws IOException {

        if (url == null || !url.startsWith("/uploads/")) {
            throw new IllegalArgumentException("URL invalide : seules les images de /uploads/ peuvent être supprimées");
        }

        String fileName = url.substring("/uploads/".length());
        Path target = uploadDir.resolve(fileName).normalize();
        if (!target.startsWith(uploadDir)) {
            throw new IllegalArgumentException("URL invalide");
        }

        boolean deleted = Files.deleteIfExists(target);
        log.info(deleted ? "Image supprimée : {}" : "Image déjà absente : {}", target);

        return new MessageResponse(true, deleted ? "Image supprimée" : "Image déjà absente");
    }
}