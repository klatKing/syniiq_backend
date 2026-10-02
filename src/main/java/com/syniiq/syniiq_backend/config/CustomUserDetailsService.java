package com.syniiq.syniiq_backend.config;

import com.syniiq.syniiq_backend.model.AppUser;
import com.syniiq.syniiq_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AppUser user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable : " + email));

        return User.withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRoleSysteme().name())   // ex: ROLE_ADMIN
                .disabled(!user.isActif())
                .build();
    }
}