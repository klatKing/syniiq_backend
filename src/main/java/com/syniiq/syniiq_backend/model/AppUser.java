package com.syniiq.syniiq_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** Un compte utilisateur (l'administrateur du site). */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(nullable = false, length = 30)
    private String telephone;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /** Mot de passe chiffré (BCrypt), jamais en clair. */
    @Column(nullable = false, length = 100)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_systeme", nullable = false, length = 30)
    private RoleSysteme roleSysteme;

    @Column(name = "photo_profil", length = 500)
    private String photoProfil;

    @Column(nullable = false)
    private boolean actif = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}