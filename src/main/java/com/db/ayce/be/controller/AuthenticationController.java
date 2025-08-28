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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.db.ayce.be.dto.LoginRequest;
import com.db.ayce.be.entity.Utente;
import com.db.ayce.be.repository.UtenteRepository;
import com.db.ayce.be.security.jwt.JwtService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UtenteRepository utenteRepository;

    public AuthenticationController(AuthenticationManager authManager,
                                    JwtService jwtService,
                                    UtenteRepository utenteRepository) {
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.utenteRepository = utenteRepository;
    }
    
    @PostMapping("/login-guest")
    public ResponseEntity<?> loginGuest(HttpServletResponse response) {
        Utente guest = utenteRepository.findByUsername("guest")
                .orElseThrow(() -> new RuntimeException("Utente guest non trovato"));

        String token = jwtService.generateToken(guest);

        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60); // 1 ora
        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                request.getUsername(), request.getPassword());

        Utente utente = utenteRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        authManager.authenticate(auth);

        String token = jwtService.generateToken(utente);

        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60); // 1 ora
        response.addCookie(cookie);

        return ResponseEntity.ok().build(); // NON restituiamo il token
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("token", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // Cancella subito
        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            Utente utente = utenteRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Utente non trovato"));

            Map<String, Object> userInfo = Map.of(
                    "id", utente.getId(),
                    "username", utente.getUsername(),
                    "livello", utente.getLivello()
            );

            return ResponseEntity.ok(userInfo);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    
}
