package com.example.smartcampus.controller;

import com.example.smartcampus.dto.CompanyProfileRequest;
import com.example.smartcampus.dto.CompanyApplicationView;
import com.example.smartcampus.dto.JobRequest;
import com.example.smartcampus.dto.StatusUpdateRequest;
import com.example.smartcampus.entity.*;
import com.example.smartcampus.repository.UserRepository;
import com.example.smartcampus.service.CompanyService;
import com.example.smartcampus.service.DashboardService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/company")
public class CompanyController {

    private final CompanyService companyService;
    private final UserRepository userRepository;
    private final DashboardService dashboardService;

    public CompanyController(CompanyService companyService, UserRepository userRepository, DashboardService dashboardService) {
        this.companyService = companyService;
        this.userRepository = userRepository;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<CompanyProfile> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(companyService.getProfile(user));
    }

    @PostMapping("/profile")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<CompanyProfile> saveProfile(@AuthenticationPrincipal UserDetails userDetails,
                                                    @Valid @RequestBody CompanyProfileRequest request) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(companyService.upsertProfile(user, request));
    }

    @GetMapping("/jobs")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<?> getJobs(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(companyService.getJobsForCompany(user));
    }

    @PostMapping("/jobs")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Job> createJob(@AuthenticationPrincipal UserDetails userDetails,
                                        @Valid @RequestBody JobRequest request) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(companyService.createJob(user, request));
    }

    @PutMapping("/jobs/{jobId}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Job> updateJob(@AuthenticationPrincipal UserDetails userDetails,
                                        @PathVariable Long jobId,
                                        @Valid @RequestBody JobRequest request) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(companyService.updateJob(user, jobId, request));
    }

    @GetMapping("/applications")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<List<CompanyApplicationView>> getApplications(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(companyService.getApplicationsForCompany(user));
    }

    @PatchMapping("/applications/{applicationId}/status")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<JobApplication> updateStatus(@AuthenticationPrincipal UserDetails userDetails,
                                                      @PathVariable Long applicationId,
                                                      @Valid @RequestBody StatusUpdateRequest request) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(companyService.updateApplicationStatus(user, applicationId, request.getStatus()));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Map<String, Object>> getDashboard(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(dashboardService.getCompanyDashboard(user));
    }
}
