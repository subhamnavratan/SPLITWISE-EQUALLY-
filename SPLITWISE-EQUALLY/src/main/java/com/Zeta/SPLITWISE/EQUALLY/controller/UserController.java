package com.Zeta.SPLITWISE.EQUALLY.controller;

import com.Zeta.SPLITWISE.EQUALLY.model.User;
import com.Zeta.SPLITWISE.EQUALLY.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ------------------------------------------
    // REGISTER USER
    // ------------------------------------------
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

    // ------------------------------------------
    // LOGIN USER
    // ------------------------------------------
    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String identifier = loginRequest.get("identifier");
            String password = loginRequest.get("password");

            if (identifier == null || password == null) {
                throw new IllegalArgumentException("Identifier and password are required.");
            }

            User user = userService.login(identifier, password);
            user.setPassword(null);

            return ResponseEntity.ok(user);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(null);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    // ------------------------------------------
    // ✅ NEW: GET USER BY USER ID (Needed for payments)
    // ------------------------------------------
    @GetMapping("/id/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable String userId) {
        try {
            User user = userService.getUserById(userId);
            user.setPassword(null);
            return ResponseEntity.ok(user);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
