package com.syniiq.syniiq_backend.config;

import com.syniiq.syniiq_backend.model.AppUser;
import com.syniiq.syniiq_backend.model.RoleSysteme;
import com.syniiq.syniiq_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.nom}")
    private String adminNom;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.phone}")
    private String adminPhone;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        String email = adminEmail.trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            log.info("Compte admin déjà existant : {}", email);
            return;
        }

        AppUser admin = new AppUser();
        admin.setNom(adminNom);
        admin.setEmail(email);
        admin.setTelephone(adminPhone);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRoleSysteme(RoleSysteme.ROLE_ADMIN);
        admin.setActif(true);
        userRepository.save(admin);

        log.info("Compte admin créé : {}", email);
    }
}