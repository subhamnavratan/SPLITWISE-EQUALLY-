package com.Zeta.SPLITWISE.EQUALLY.controller;

import com.Zeta.SPLITWISE.EQUALLY.model.User;
import com.Zeta.SPLITWISE.EQUALLY.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map; // REQUIRED: For reading the JSON body during login
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Registration (Signup) - remains POST /register
    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody User user) {
        try {
            User savedUser = userService.registerUser(user);
            savedUser.setPassword(null);
            return ResponseEntity.ok(savedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    // CRITICAL FIX: Login now uses POST and accepts credentials in the body
    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String identifier = loginRequest.get("identifier");
            String password = loginRequest.get("password");

            if (identifier == null || password == null) {
                throw new IllegalArgumentException("Identifier and password are required.");
            }

            // Call the service with both identifier and password
            User user = userService.login(identifier, password);

            // Success: Clear password before returning the user object
            user.setPassword(null);
            return ResponseEntity.ok(user);

        } catch (IllegalArgumentException e) {
            // This catches "Invalid credentials" from the service
            return ResponseEntity.status(401).body(null);
        } catch (NoSuchElementException e) {
            // User not found
            return ResponseEntity.status(404).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    // NOTE: The old @GetMapping("/login/{identifier}") method is removed for security.
}