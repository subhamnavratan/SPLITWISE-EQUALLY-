package com.Zeta.SPLITWISE.EQUALLY.service;

import com.Zeta.SPLITWISE.EQUALLY.model.User;
import com.Zeta.SPLITWISE.EQUALLY.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ------------------------------------------
    // REGISTER
    // ------------------------------------------
    public User registerUser(User user) {

        if (user.getPassword() == null || user.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }

        if (user.getEmail() == null || !user.getEmail().toLowerCase().endsWith("@gmail.com")) {
            throw new IllegalArgumentException("Email must end with @gmail.com.");
        }

        if (user.getPhone() == null || String.valueOf(user.getPhone()).length() != 10) {
            throw new IllegalArgumentException("Phone number must be exactly 10 digits.");
        }

        if (userRepository.findByPhone(user.getPhone()).isPresent()) {
            throw new IllegalArgumentException("User with this phone number already exists.");
        }

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User with this email already exists.");
        }

        String hashed = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashed);

        return userRepository.save(user);
    }

    // ------------------------------------------
    // LOGIN
    // ------------------------------------------
    public User login(String identifier, String rawPassword) {
        User user;

        if (identifier.contains("@")) {
            user = userRepository.findByEmail(identifier)
                    .orElseThrow(() -> new NoSuchElementException("User not found with email: " + identifier));
        } else {
            try {
                Long phone = Long.parseLong(identifier);
                user = userRepository.findByPhone(phone)
                        .orElseThrow(() -> new NoSuchElementException("User not found with phone: " + identifier));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid login format.");
            }
        }

        if (passwordEncoder.matches(rawPassword, user.getPassword())) {
            return user;
        } else {
            throw new IllegalArgumentException("Invalid credentials.");
        }
    }

    // ------------------------------------------
    // ✅ NEW: GET USER BY USER ID
    // ------------------------------------------
    public User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));
    }
}
