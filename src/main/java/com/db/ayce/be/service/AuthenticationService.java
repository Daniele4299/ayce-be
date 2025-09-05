package com.db.ayce.be.service;

import com.db.ayce.be.dto.LoginRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

public interface AuthenticationService {
    ResponseEntity<?> login(LoginRequest request, HttpServletResponse response);
    ResponseEntity<?> loginTavolo(Integer tavoloNum, HttpServletResponse response);
    ResponseEntity<?> logout(HttpServletResponse response);
    ResponseEntity<?> getCurrentUser();
}
