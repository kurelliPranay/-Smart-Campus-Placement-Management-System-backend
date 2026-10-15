package com.example.smartcampus.controller;

import com.example.smartcampus.entity.Job;
import com.example.smartcampus.repository.JobRepository;
import com.example.smartcampus.service.JobApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class JobController {

    private final JobRepository jobRepository;
    private final JobApplicationService jobApplicationService;

    public JobController(JobRepository jobRepository, JobApplicationService jobApplicationService) {
        this.jobRepository = jobRepository;
        this.jobApplicationService = jobApplicationService;
    }

    @GetMapping("/jobs")
    public ResponseEntity<List<Job>> getPublishedJobs() {
        return ResponseEntity.ok(jobRepository.findByStatus(com.example.smartcampus.entity.JobStatus.PUBLISHED));
    }

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<Job> getJobById(@PathVariable Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        return ResponseEntity.ok(job);
    }
}
