package com.example.smartcampus.service;

import com.example.smartcampus.dto.RegistrationRequest;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private UserRepository userRepository;
    private AuthService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        service = new AuthService(
                userRepository,
                mock(PasswordEncoder.class),
                mock(AuthenticationManager.class)
        );
    }

    @Test
    void studentRegistrationPersistsCampusDetails() {
        RegistrationRequest request = new RegistrationRequest();
        request.setFullName("Aisha Kumar");
        request.setEmail("aisha@campus.edu");
        request.setPassword("secret1");
        request.setRole("STUDENT");
        request.setBranch("CSE");
        request.setGraduationYear(2026);
        request.setCollegeName("National Institute of Technology");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User savedUser = service.register(request);

        assertEquals("CSE", savedUser.getBranch());
        assertEquals(2026, savedUser.getGraduationYear());
        assertEquals("National Institute of Technology", savedUser.getCollegeName());
    }

    @Test
    void companyRegistrationDoesNotSetStudentDetails() {
        RegistrationRequest request = new RegistrationRequest();
        request.setFullName("Aisha Kumar");
        request.setEmail("aisha@company.com");
        request.setPassword("secret1");
        request.setRole("COMPANY");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User savedUser = service.register(request);

        org.junit.jupiter.api.Assertions.assertNull(savedUser.getBranch());
        org.junit.jupiter.api.Assertions.assertNull(savedUser.getGraduationYear());
        org.junit.jupiter.api.Assertions.assertNull(savedUser.getCollegeName());
    }
}
