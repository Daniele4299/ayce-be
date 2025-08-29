package com.db.ayce.be.utils;

import java.util.Arrays;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.db.ayce.be.entity.Utente;
import com.db.ayce.be.repository.UtenteRepository;

import io.jsonwebtoken.Claims;

@Component
public class AuthUtils {

    private final UtenteRepository utenteRepository;

    public AuthUtils(UtenteRepository utenteRepository) {
        this.utenteRepository = utenteRepository;
    }

    /**
     * Restituisce l'utente corrente se autenticato e se ha uno dei ruoli autorizzati.
     * Può gestire anche CLIENT (tavolo)
     */
    public Utente getCurrentUserOrThrow(String... allowedRoles) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof Claims claims) { // CLIENT tavolo
            String role = claims.get("role", String.class);
            if (Arrays.stream(allowedRoles).noneMatch(r -> r.equals(role))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accesso negato");
            }
            return null; // o eventualmente un DTO client-specific
        } else if (principal instanceof UserDetails userDetails) {
            Utente utente = utenteRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utente non trovato"));

            String ruoloUtente = Constants.getRoleName(utente.getLivello());
            if (Arrays.stream(allowedRoles).noneMatch(r -> r.equals(ruoloUtente))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Utente non autorizzato");
            }
            return utente;
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utente non autenticato");
    }
}
