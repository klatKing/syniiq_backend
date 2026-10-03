package com.syniiq.syniiq_backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI syniiqOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Syniiq API")
                        .version("1.0.0")
                        .description("""
                                API du site vitrine de l'entreprise **Syniiq** : services, projets, équipe et avis clients.

                                ## Format des réponses
                                Toutes les réponses, succès comme erreurs, ont la même forme :
                                `{ "success": true|false, "message": "...", "<données>": ... }`.
                                La clé des données porte le nom de la ressource (`user`, `service`, `services`, \
                                `project`, `projects`, `employee`, `employees`, `testimonial`, `testimonials`).

                                ## Accès
                                - **Lecture (GET)** : publique, aucune connexion nécessaire.
                                - **Création, modification, suppression, upload** : réservés à l'administrateur,
                                  avec un token JWT. Appelez `POST /api/auth/login`, copiez la valeur du champ `token`,
                                  cliquez sur **Authorize** et collez-la (sans le mot « Bearer »).

                                ## Images
                                1. Envoyer le fichier avec `POST /api/files/upload`. La réponse contient une `url`.
                                2. Enregistrer cette `url` dans `photoUrl`, `imageUrl` ou `icon`.
                                3. Afficher l'image via `http://localhost:8080` + `url`.

                                ## Temps réel (WebSocket, non visible dans Swagger)
                                Connexion STOMP sur `ws://localhost:8080/ws`. Abonnements possibles :
                                `/topic/services`, `/topic/projects`, `/topic/employees`, `/topic/testimonials`.
                                Chaque message est `{ "action": "CREATED|UPDATED|DELETED", "data": ... }` :
                                `data` est l'objet complet pour CREATED et UPDATED, et l'identifiant pour DELETED.
                                """)
                        .contact(new Contact().name("Syniiq").email("admin@syniiq.com")))
             .servers(List.of(new Server().url("/").description("Serveur courant")))
                               .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT obtenu via POST /api/auth/login")));
    }
}