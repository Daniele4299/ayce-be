package com.db.ayce.be.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.db.ayce.be.dto.LoginRequest;
import com.db.ayce.be.entity.Sessione;
import com.db.ayce.be.entity.Tavolo;
import com.db.ayce.be.entity.Utente;
import com.db.ayce.be.repository.UtenteRepository;
import com.db.ayce.be.security.jwt.JwtService;
import com.db.ayce.be.service.SessioneService;
import com.db.ayce.be.service.TavoloService;
import com.db.ayce.be.utils.Constants;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UtenteRepository utenteRepository;
    private final TavoloService tavoloService;
    private final SessioneService sessioneService;

    public AuthenticationController(AuthenticationManager authManager,
                                    JwtService jwtService,
                                    UtenteRepository utenteRepository,
                                    TavoloService tavoloService,
                                    SessioneService sessioneService) {
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.utenteRepository = utenteRepository;
        this.tavoloService = tavoloService;
        this.sessioneService = sessioneService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                request.getUsername(), request.getPassword());
        authManager.authenticate(auth);

        Utente utente = utenteRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        String token = jwtService.generateToken(utente);

        // Sovrascrive qualsiasi token esistente
        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60); // 1 ora
        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/login-tavolo/{tavoloNum}")
    public ResponseEntity<?> loginTavolo(@PathVariable Integer tavoloNum, HttpServletResponse response) {
        Tavolo tavolo = tavoloService.findByNumero(tavoloNum);
        if (tavolo == null || !Boolean.TRUE.equals(tavolo.getAttivo())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Tavolo non trovato o non attivo"));
        }

        Sessione sessioneAttiva = sessioneService.findAll().stream()
                .filter(s -> s.getTavolo().getId().equals(tavolo.getId()))
                .filter(s -> "ATTIVA".equals(s.getStato()))
                .findFirst()
                .orElse(null);

        if (sessioneAttiva == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Nessuna sessione attiva per questo tavolo"));
        }

        String token = jwtService.generateClientToken(sessioneAttiva);

        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 7);
        response.addCookie(cookie);

        return ResponseEntity.ok(Map.of(
                "tavoloId", tavolo.getId(),
                "numeroTavolo", tavolo.getNumero(),
                "sessioneId", sessioneAttiva.getId(),
                "isAyce", sessioneAttiva.getIsAyce()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("token", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof Claims claims && Constants.ROLE_CLIENT.equals(claims.get("role", String.class))) {
            Long sessioneId = claims.get("sessioneId", Long.class);
            Sessione sessione = sessioneService.findById(sessioneId);

            if (sessione == null || !"ATTIVA".equals(sessione.getStato())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            return ResponseEntity.ok(Map.of(
                "role", Constants.ROLE_CLIENT,
                "tavoloId", sessione.getTavolo().getId(),
                "tavoloNum", sessione.getTavolo().getNumero(),
                "sessioneId", sessione.getId(),
                "isAyce", sessione.getIsAyce()
            ));
        } else if (principal instanceof UserDetails userDetails) {
            // Utente reale DB
            Utente utente = utenteRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Utente non trovato"));

            return ResponseEntity.ok(Map.of(
                    "role", Constants.getRoleName(utente.getLivello()), // usa la mappa centrale
                    "id", utente.getId(),
                    "username", utente.getUsername()
            ));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
