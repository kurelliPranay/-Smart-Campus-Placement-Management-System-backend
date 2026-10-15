package com.example.smartcampus.controller;

import com.example.smartcampus.dto.StudentProfileRequest;
import com.example.smartcampus.entity.JobApplication;
import com.example.smartcampus.entity.StudentProfile;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.repository.UserRepository;
import com.example.smartcampus.service.DashboardService;
import com.example.smartcampus.service.JobApplicationService;
import com.example.smartcampus.service.StudentProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final StudentProfileService studentProfileService;
    private final DashboardService dashboardService;
    private final UserRepository userRepository;
    private final JobApplicationService jobApplicationService;

    public StudentController(StudentProfileService studentProfileService,
                            DashboardService dashboardService,
                            UserRepository userRepository,
                            JobApplicationService jobApplicationService) {
        this.studentProfileService = studentProfileService;
        this.dashboardService = dashboardService;
        this.userRepository = userRepository;
        this.jobApplicationService = jobApplicationService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentProfile> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(studentProfileService.getProfileByUser(user));
    }

    @PostMapping("/profile")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentProfile> saveProfile(@AuthenticationPrincipal UserDetails userDetails,
                                                     @Valid @RequestBody StudentProfileRequest request) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(studentProfileService.upsertProfile(user, request));
    }

    @GetMapping("/jobs")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getAvailableJobs(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(studentProfileService.getAllAvailableJobs());
    }

    @GetMapping("/jobs/eligible")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getEligibleJobs(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(studentProfileService.getEligibleJobs(user));
    }

    @GetMapping("/jobs/{jobId}/eligibility")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> checkEligibility(@AuthenticationPrincipal UserDetails userDetails,
                                                              @PathVariable Long jobId) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(jobApplicationService.getEligibility(user, jobId));
    }

    @PostMapping("/jobs/{jobId}/apply")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<JobApplication> applyForJob(@AuthenticationPrincipal UserDetails userDetails,
                                                    @PathVariable Long jobId) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(jobApplicationService.applyForJob(user, jobId));
    }

    @GetMapping("/applications")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<JobApplication>> getApplications(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(jobApplicationService.getApplicationsForStudent(user));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> getDashboard(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(dashboardService.getStudentDashboard(user));
    }
}
