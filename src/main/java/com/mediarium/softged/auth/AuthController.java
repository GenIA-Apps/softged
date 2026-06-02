package com.mediarium.softged.auth;

import com.mediarium.softged.shared.exception.ResourceNotFoundException;
import com.mediarium.softged.shared.security.FirebasePrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @GetMapping("/api/me")
    public FirebasePrincipal me(Authentication authentication) {
        return (FirebasePrincipal) authentication.getPrincipal();
    }

    @GetMapping("/api/test-error")
    public void testError() {
        throw new ResourceNotFoundException("Test resource not found");
    }
}