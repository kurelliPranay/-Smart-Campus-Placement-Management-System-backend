package com.example.smartcampus.service;

import com.example.smartcampus.dto.CompanyProfileRequest;
import com.example.smartcampus.dto.CompanyApplicationView;
import com.example.smartcampus.dto.JobRequest;
import com.example.smartcampus.entity.*;
import com.example.smartcampus.exception.ResourceNotFoundException;
import com.example.smartcampus.repository.CompanyProfileRepository;
import com.example.smartcampus.repository.JobApplicationRepository;
import com.example.smartcampus.repository.JobRepository;
import com.example.smartcampus.repository.StudentProfileRepository;
import com.example.smartcampus.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CompanyService {

    private final CompanyProfileRepository companyProfileRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final StudentProfileRepository studentProfileRepository;

    public CompanyService(CompanyProfileRepository companyProfileRepository,
                         JobRepository jobRepository,
                         UserRepository userRepository,
                         JobApplicationRepository jobApplicationRepository,
                         StudentProfileRepository studentProfileRepository) {
        this.companyProfileRepository = companyProfileRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.studentProfileRepository = studentProfileRepository;
    }

    @Transactional
    public CompanyProfile upsertProfile(User user, CompanyProfileRequest request) {
        if (user.getRole() != UserRole.COMPANY) {
            throw new IllegalArgumentException("Only companies can update company profiles");
        }

        CompanyProfile profile = companyProfileRepository.findByUser(user)
                .orElse(new CompanyProfile());
        profile.setUser(user);
        profile.setCompanyName(request.getCompanyName());
        profile.setIndustry(request.getIndustry());
        profile.setDescription(request.getDescription());
        profile.setWebsite(request.getWebsite());
        profile.setContactEmail(request.getContactEmail());
        return companyProfileRepository.save(profile);
    }

    public CompanyProfile getProfile(User user) {
        return companyProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Company profile not found"));
    }

    public Job createJob(User user, JobRequest request) {
        StudentProfileService.parseApplicationDeadline(request.getApplicationDeadline());
        CompanyProfile company = companyProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Company profile not found"));

        Job job = new Job();
        job.setCompany(company);
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setRequiredSkills(request.getRequiredSkills());
        job.setEligibleBranches(request.getEligibleBranches());
        job.setMinCgpa(request.getMinCgpa());
        job.setApplicationDeadline(request.getApplicationDeadline());
        job.setStatus(parseStatus(request.getStatus()));
        return jobRepository.save(job);
    }

    public Job updateJob(User user, Long jobId, JobRequest request) {
        StudentProfileService.parseApplicationDeadline(request.getApplicationDeadline());
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        if (!job.getCompany().getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You can only edit your own jobs");
        }
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setRequiredSkills(request.getRequiredSkills());
        job.setEligibleBranches(request.getEligibleBranches());
        job.setMinCgpa(request.getMinCgpa());
        job.setApplicationDeadline(request.getApplicationDeadline());
        job.setStatus(parseStatus(request.getStatus()));
        return jobRepository.save(job);
    }

    public List<Job> getJobsForCompany(User user) {
        CompanyProfile company = companyProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Company profile not found"));
        return jobRepository.findByCompany(company);
    }

    @Transactional(readOnly = true)
    public List<CompanyApplicationView> getApplicationsForCompany(User user) {
        CompanyProfile company = companyProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Company profile not found"));
        List<JobApplication> applications = company.getJobs().stream()
                .flatMap(job -> job.getApplications().stream())
                .toList();
        if (applications.isEmpty()) {
            return List.of();
        }

        List<User> applicants = applications.stream()
                .map(JobApplication::getApplicant)
                .distinct()
                .toList();
        Map<Long, StudentProfile> profilesByUserId = studentProfileRepository.findByUserIn(applicants).stream()
                .collect(Collectors.toMap(profile -> profile.getUser().getId(), Function.identity()));

        return applications.stream()
                .map(application -> {
                    User applicant = application.getApplicant();
                    StudentProfile profile = profilesByUserId.get(applicant.getId());
                    CompanyApplicationView.StudentProfileView profileView = profile == null ? null
                            : new CompanyApplicationView.StudentProfileView(
                                    profile.getEducation(),
                                    profile.getBranch(),
                                    profile.getCgpa(),
                                    profile.getSkills(),
                                    profile.getProjects(),
                                    profile.getCertifications(),
                                    profile.getResumeUrl()
                            );
                    return new CompanyApplicationView(
                            application.getId(),
                            application.getJob().getId(),
                            new CompanyApplicationView.ApplicantView(
                                    applicant.getFullName(),
                                    applicant.getEmail(),
                                    profileView
                            ),
                            application.getJob().getTitle(),
                            application.getStatus(),
                            application.getAppliedAt(),
                            application.isEligible()
                    );
                })
                .toList();
    }

    public JobApplication updateApplicationStatus(User user, Long applicationId, String statusText) {
        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if (!application.getJob().getCompany().getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You can only update applications for your own jobs");
        }

        ApplicationStatus newStatus = parseApplicationStatus(statusText);
        application.setStatus(newStatus);
        return jobApplicationRepository.save(application);
    }

    public String generateCompanyDashboard(User user) {
        return "Dashboard ready";
    }

    private JobStatus parseStatus(String statusText) {
        if (statusText == null || statusText.isBlank()) {
            return JobStatus.DRAFT;
        }
        return JobStatus.valueOf(statusText.trim().toUpperCase());
    }

    private ApplicationStatus parseApplicationStatus(String statusText) {
        if (statusText == null || statusText.isBlank()) {
            return ApplicationStatus.APPLIED;
        }
        return ApplicationStatus.valueOf(statusText.trim().toUpperCase());
    }
}
