package com.example.smartcampus.service;

import com.example.smartcampus.dto.LoginRequest;
import com.example.smartcampus.dto.RegistrationRequest;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.entity.UserRole;
import com.example.smartcampus.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public User register(RegistrationRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User already exists with this email");
        }

        String normalizedRole = request.getRole() == null ? "STUDENT" : request.getRole().trim().toUpperCase();
        if ("ADMIN".equals(normalizedRole)) {
            throw new IllegalArgumentException("Public registration cannot create admin accounts");
        }

        UserRole role = UserRole.valueOf(normalizedRole);
        if (role != UserRole.STUDENT && role != UserRole.COMPANY) {
            throw new IllegalArgumentException("Only STUDENT and COMPANY roles are allowed for public registration");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setEnabled(true);
        if (role == UserRole.STUDENT) {
            user.setBranch(request.getBranch());
            user.setGraduationYear(request.getGraduationYear());
            user.setCollegeName(request.getCollegeName());
        }
        return userRepository.save(user);
    }

    public User login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        return userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
