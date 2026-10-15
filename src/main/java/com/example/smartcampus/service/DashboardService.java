package com.example.smartcampus.service;

import com.example.smartcampus.entity.*;
import com.example.smartcampus.repository.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final CompanyProfileRepository companyProfileRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final JobRepository jobRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final StudentProfileService studentProfileService;

    public DashboardService(UserRepository userRepository,
                           CompanyProfileRepository companyProfileRepository,
                           StudentProfileRepository studentProfileRepository,
                           JobRepository jobRepository,
                           JobApplicationRepository jobApplicationRepository,
                           StudentProfileService studentProfileService) {
        this.userRepository = userRepository;
        this.companyProfileRepository = companyProfileRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.jobRepository = jobRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.studentProfileService = studentProfileService;
    }

    public Map<String, Object> getStudentDashboard(User user) {
        int profileCompletion = studentProfileService.getProfileCompletion(user);
        List<Job> publishedJobs = jobRepository.findByStatus(JobStatus.PUBLISHED);
        long applications = jobApplicationRepository.findByApplicant(user).size();

        int eligibleJobs = 0;
        List<StudentProfileService.Recommendation> recommendations = List.of();
        StudentProfile profile = studentProfileRepository.findByUser(user).orElse(null);
        if (profile != null) {
            for (Job job : publishedJobs) {
                if (studentProfileService.isEligible(profile, job).isEligible()) {
                    eligibleJobs++;
                }
            }
            recommendations = studentProfileService.getRecommendations(user);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("profileCompletion", profileCompletion);
        response.put("availableJobs", publishedJobs.size());
        response.put("eligibleJobs", eligibleJobs);
        response.put("applications", applications);
        response.put("recommendations", recommendations);
        return response;
    }

    public Map<String, Object> getAdminDashboard() {
        Map<String, Object> response = new HashMap<>();
        response.put("totalStudents", userRepository.countByRole(UserRole.STUDENT));
        response.put("totalCompanies", userRepository.countByRole(UserRole.COMPANY));
        response.put("publishedJobs", jobRepository.findByStatus(JobStatus.PUBLISHED).size());
        response.put("totalApplications", jobApplicationRepository.count());
        response.put("selectedCandidates", jobApplicationRepository.countByStatus(ApplicationStatus.SELECTED));
        return response;
    }

    public Map<String, Object> getCompanyDashboard(User user) {
        CompanyProfile company = companyProfileRepository.findByUser(user).orElse(null);
        Map<String, Object> response = new HashMap<>();
        response.put("companyJobs", company == null ? 0 : company.getJobs().size());
        response.put("totalApplicants", company == null ? 0 : company.getJobs().stream().mapToLong(job -> job.getApplications().size()).sum());
        response.put("shortlisted", company == null ? 0 : company.getJobs().stream().flatMap(job -> job.getApplications().stream()).filter(app -> app.getStatus() == ApplicationStatus.SHORTLISTED).count());
        response.put("interviews", company == null ? 0 : company.getJobs().stream().flatMap(job -> job.getApplications().stream()).filter(app -> app.getStatus() == ApplicationStatus.INTERVIEW).count());
        return response;
    }
}
