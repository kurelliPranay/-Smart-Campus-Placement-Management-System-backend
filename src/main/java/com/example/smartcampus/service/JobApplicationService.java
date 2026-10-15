package com.example.smartcampus.service;

import com.example.smartcampus.entity.ApplicationStatus;
import com.example.smartcampus.entity.Job;
import com.example.smartcampus.entity.JobApplication;
import com.example.smartcampus.entity.JobStatus;
import com.example.smartcampus.entity.StudentProfile;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.entity.UserRole;
import com.example.smartcampus.exception.ResourceNotFoundException;
import com.example.smartcampus.repository.JobApplicationRepository;
import com.example.smartcampus.repository.JobRepository;
import com.example.smartcampus.repository.StudentProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final JobRepository jobRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final StudentProfileService studentProfileService;

    public JobApplicationService(JobApplicationRepository jobApplicationRepository,
                                JobRepository jobRepository,
                                StudentProfileRepository studentProfileRepository,
                                StudentProfileService studentProfileService) {
        this.jobApplicationRepository = jobApplicationRepository;
        this.jobRepository = jobRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.studentProfileService = studentProfileService;
    }

    @Transactional
    public JobApplication applyForJob(User user, Long jobId) {
        if (user.getRole() != UserRole.STUDENT) {
            throw new IllegalArgumentException("Only students can apply for jobs");
        }

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (job.getStatus() != JobStatus.PUBLISHED) {
            throw new IllegalArgumentException("This job is not currently open for applications");
        }
        if (StudentProfileService.isDeadlinePassed(job)) {
            throw new IllegalArgumentException("The application deadline for this job has passed");
        }

        StudentProfile profile = studentProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Complete your student profile before applying"));

        if (jobApplicationRepository.findByApplicantAndJob(user, job).isPresent()) {
            throw new IllegalArgumentException("You have already applied to this job");
        }

        StudentProfileService.EligibilityResult result = studentProfileService.isEligible(profile, job);
        if (!result.isEligible()) {
            throw new IllegalArgumentException("You are not eligible for this job: " + String.join(", ", result.getReasons()));
        }

        JobApplication application = new JobApplication();
        application.setApplicant(user);
        application.setJob(job);
        application.setStatus(ApplicationStatus.APPLIED);
        application.setAppliedAt(LocalDateTime.now().toString());
        application.setEligible(true);
        return jobApplicationRepository.save(application);
    }

    public List<JobApplication> getApplicationsForStudent(User user) {
        return jobApplicationRepository.findByApplicant(user);
    }

    public Map<String, Object> getEligibility(User user, Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        StudentProfile profile = studentProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Complete your student profile first"));

        StudentProfileService.EligibilityResult result = studentProfileService.isEligible(profile, job);
        Map<String, Object> response = new HashMap<>();
        response.put("eligible", result.isEligible());
        response.put("reasons", result.getReasons());
        response.put("missingSkills", new ArrayList<>(result.getMissingSkills()));
        return response;
    }
}
