package com.Zeta.SPLITWISE.EQUALLY.service;

import com.Zeta.SPLITWISE.EQUALLY.model.User;
import com.Zeta.SPLITWISE.EQUALLY.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
// NOTE: PasswordEncoder dependency is still injected but its methods are ignored for hashing.
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Kept for constructor completion

    // Inject both dependencies (though encoder is now unused for its core purpose)
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }



// ... other necessary imports (User, UserRepository, PasswordEncoder, etc.)

    public User registerUser(User user) {
        // --- 1. Validation Checks ---

        // Password Validation
        if (user.getPassword() == null || user.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }

        // Email Format Validation
        if (user.getEmail() == null || !user.getEmail().toLowerCase().endsWith("@gmail.com")) {
            throw new IllegalArgumentException("Email must end with @gmail.com.");
        }

        // 💥 NEW: Phone Number Length Validation (10 digits only)
        if (user.getPhone() == null || String.valueOf(user.getPhone()).length() != 10) {
            throw new IllegalArgumentException("Phone number must be exactly 10 digits.");
        }

        // --- 2. Duplicate Checks ---

        // Check for duplicate phone number
        if (userRepository.findByPhone(user.getPhone()).isPresent()) {
            throw new IllegalArgumentException("User with this phone number already exists.");
        }

        // Check for duplicate email (Original check)
        if (user.getEmail() != null && userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User with this email already exists.");
        }

        // --- 3. Hashing and Saving ---

        String hashed = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashed); // Set the HASHED password

        User savedUser = userRepository.save(user);

        System.out.println("✅ User registered successfully: " + savedUser.getUserId());



        return savedUser;
    }
    // Inside UserService.java

    public User login(String loginIdentifier, String rawPassword) {
        User user = null;

        // 1. Retrieve User by Identifier (Logic to find user by email or phone)
        if (loginIdentifier.contains("@")) {
            user = userRepository.findByEmail(loginIdentifier)
                    .orElseThrow(() -> new NoSuchElementException("User not found with email: " + loginIdentifier));
        } else {
            try {
                Long phone = Long.parseLong(loginIdentifier);
                user = userRepository.findByPhone(phone)
                        .orElseThrow(() -> new NoSuchElementException("User not found with phone: " + phone));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid login format. Must be a valid phone number or email.");
            }
        }

        // 2. CRITICAL FIX: Verify the raw password against the stored hash
        // The passwordEncoder handles the hashing and comparison internally.
        if (passwordEncoder.matches(rawPassword, user.getPassword())) {
            return user; // Authentication Success
        } else {
            // Logically, this means either the user or the password was wrong.
            throw new IllegalArgumentException("Invalid credentials.");
        }
    }
}