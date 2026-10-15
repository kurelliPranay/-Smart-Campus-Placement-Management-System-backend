package com.example.smartcampus.controller;

import com.example.smartcampus.entity.StudentProfile;
import com.example.smartcampus.repository.CompanyProfileRepository;
import com.example.smartcampus.repository.JobApplicationRepository;
import com.example.smartcampus.repository.JobRepository;
import com.example.smartcampus.repository.StudentProfileRepository;
import com.example.smartcampus.repository.UserRepository;
import com.example.smartcampus.service.DashboardService;
import com.example.smartcampus.service.StudentProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminControllerTest {

    private StudentProfileRepository studentProfileRepository;
    private JobRepository jobRepository;
    private AdminController controller;

    @BeforeEach
    void setUp() {
        UserRepository userRepository = mock(UserRepository.class);
        CompanyProfileRepository companyProfileRepository = mock(CompanyProfileRepository.class);
        studentProfileRepository = mock(StudentProfileRepository.class);
        jobRepository = mock(JobRepository.class);
        JobApplicationRepository jobApplicationRepository = mock(JobApplicationRepository.class);
        StudentProfileService studentProfileService = new StudentProfileService(
                studentProfileRepository, userRepository, jobRepository, jobApplicationRepository
        );
        DashboardService dashboardService = new DashboardService(
                userRepository,
                companyProfileRepository,
                studentProfileRepository,
                jobRepository,
                jobApplicationRepository,
                studentProfileService
        );
        controller = new AdminController(
                dashboardService,
                studentProfileRepository,
                companyProfileRepository,
                jobRepository,
                jobApplicationRepository
        );
    }

    @Test
    void studentsAreReturnedInTheRequestedStablePage() {
        StudentProfile profile = new StudentProfile();
        when(studentProfileRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(profile), PageRequest.of(1, 5), 11));

        var response = controller.getStudents(1, 5);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(List.of(profile), response.getBody().getContent());
        assertEquals(11, response.getBody().getTotalElements());
        verify(studentProfileRepository).findAll(PageRequest.of(
                1, 5, Sort.by(Sort.Direction.ASC, "id")
        ));
    }

    @Test
    void invalidPageSizeIsRejectedBeforeQuerying() {
        assertThrows(IllegalArgumentException.class, () -> controller.getStudents(0, 101));
    }

    @Test
    void negativePageNumberIsRejectedBeforeQuerying() {
        assertThrows(IllegalArgumentException.class, () -> controller.getJobs(-1, 10));
    }
}
