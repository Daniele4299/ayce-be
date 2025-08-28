package com.db.ayce.be.utils;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.db.ayce.be.entity.Utente;
import com.db.ayce.be.repository.UtenteRepository;

@Component
public class AuthUtils {

    private final UtenteRepository utenteRepository;

    public AuthUtils(UtenteRepository utenteRepository) {
        this.utenteRepository = utenteRepository;
    }

    /**
     * Restituisce l'utente corrente se autenticato e se ha uno dei ruoli autorizzati.
     */
    public Utente getCurrentUserOrThrow(Integer... ruoliAutorizzati) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof UserDetails userDetails)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utente non autenticato");
        }

        Utente utente = utenteRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utente non trovato"));

        List<Integer> ruoli = Arrays.asList(ruoliAutorizzati);
        if (!ruoli.contains(utente.getLivello())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Utente non autorizzato alla funzionalità");
        }

        return utente;
    }
}
