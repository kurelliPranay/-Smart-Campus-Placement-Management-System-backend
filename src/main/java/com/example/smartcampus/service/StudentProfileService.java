package com.example.smartcampus.service;

import com.example.smartcampus.dto.StudentProfileRequest;
import com.example.smartcampus.entity.Job;
import com.example.smartcampus.entity.JobApplication;
import com.example.smartcampus.entity.StudentProfile;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.entity.UserRole;
import com.example.smartcampus.exception.ResourceNotFoundException;
import com.example.smartcampus.repository.JobApplicationRepository;
import com.example.smartcampus.repository.JobRepository;
import com.example.smartcampus.repository.StudentProfileRepository;
import com.example.smartcampus.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class StudentProfileService {

    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final JobApplicationRepository jobApplicationRepository;

    public StudentProfileService(StudentProfileRepository studentProfileRepository,
                                UserRepository userRepository,
                                JobRepository jobRepository,
                                JobApplicationRepository jobApplicationRepository) {
        this.studentProfileRepository = studentProfileRepository;
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.jobApplicationRepository = jobApplicationRepository;
    }

    @Transactional
    public StudentProfile upsertProfile(User user, StudentProfileRequest request) {
        if (user.getRole() != UserRole.STUDENT) {
            throw new IllegalArgumentException("Only students can update their profile");
        }

        StudentProfile profile = studentProfileRepository.findByUser(user)
                .orElse(new StudentProfile());
        profile.setUser(user);
        profile.setEducation(request.getEducation());
        profile.setBranch(request.getBranch());
        profile.setCgpa(request.getCgpa());
        profile.setSkills(request.getSkills());
        profile.setProjects(request.getProjects());
        profile.setCertifications(request.getCertifications());
        profile.setResumeUrl(request.getResumeUrl());
        return studentProfileRepository.save(profile);
    }

    public StudentProfile getProfileByUser(User user) {
        return studentProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));
    }

    public List<Job> getAllAvailableJobs() {
        return jobRepository.findByStatus(com.example.smartcampus.entity.JobStatus.PUBLISHED);
    }

    public List<Job> getEligibleJobs(User user) {
        StudentProfile profile = studentProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        List<Job> publishedJobs = getAllAvailableJobs();
        List<Job> eligibleJobs = new ArrayList<>();
        for (Job job : publishedJobs) {
            if (isEligible(profile, job).isEligible()) {
                eligibleJobs.add(job);
            }
        }
        return eligibleJobs;
    }

    public EligibilityResult isEligible(StudentProfile profile, Job job) {
        List<String> reasons = new ArrayList<>();
        boolean eligible = true;

        if (isDeadlinePassed(job)) {
            eligible = false;
            reasons.add("Application deadline has passed");
        }

        if (profile.getCgpa() < job.getMinCgpa()) {
            eligible = false;
            reasons.add("CGPA below minimum requirement");
        }

        if (job.getEligibleBranches() != null && !job.getEligibleBranches().isBlank()) {
            boolean eligibleBranch = Arrays.stream(job.getEligibleBranches().split(","))
                    .map(String::trim)
                    .anyMatch(branch -> branch.equalsIgnoreCase(profile.getBranch()));
            if (!eligibleBranch) {
                eligible = false;
                reasons.add("Branch not eligible");
            }
        }

        Set<String> studentSkills = normalizeSkills(profile.getSkills());
        Set<String> requiredSkills = normalizeSkills(job.getRequiredSkills());
        Set<String> missingSkills = new HashSet<>(requiredSkills);
        missingSkills.removeAll(studentSkills);
        if (!missingSkills.isEmpty()) {
            eligible = false;
            reasons.add("Missing required skills: " + String.join(", ", missingSkills));
        }

        return new EligibilityResult(eligible, reasons, missingSkills);
    }

    public static boolean isDeadlinePassed(Job job) {
        return parseApplicationDeadline(job.getApplicationDeadline()).isBefore(LocalDate.now());
    }

    public static LocalDate parseApplicationDeadline(String deadline) {
        if (deadline == null || deadline.isBlank()) {
            throw new IllegalArgumentException("Application deadline is required");
        }
        try {
            return LocalDate.parse(deadline);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Application deadline must be a valid YYYY-MM-DD date");
        }
    }

    public List<Recommendation> getRecommendations(User user) {
        StudentProfile profile = studentProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        List<Recommendation> recommendations = new ArrayList<>();
        for (Job job : getAllAvailableJobs()) {
            EligibilityResult result = isEligible(profile, job);
            if (!result.isEligible()) {
                recommendations.add(new Recommendation(job.getTitle(), "Learn " + String.join(" and ", result.getMissingSkills()) + " to improve alignment with this job."));
            }
        }
        return recommendations;
    }

    public int getProfileCompletion(User user) {
        StudentProfile profile = studentProfileRepository.findByUser(user).orElse(null);
        if (profile == null) {
            return 0;
        }
        int completed = 0;
        if (profile.getEducation() != null && !profile.getEducation().isBlank()) completed++;
        if (profile.getBranch() != null && !profile.getBranch().isBlank()) completed++;
        if (profile.getCgpa() > 0) completed++;
        if (profile.getSkills() != null && !profile.getSkills().isBlank()) completed++;
        if (profile.getProjects() != null && !profile.getProjects().isBlank()) completed++;
        if (profile.getCertifications() != null && !profile.getCertifications().isBlank()) completed++;
        if (profile.getResumeUrl() != null && !profile.getResumeUrl().isBlank()) completed++;
        return (completed * 100) / 7;
    }

    public static Set<String> normalizeSkills(String skillText) {
        if (skillText == null || skillText.isBlank()) {
            return new HashSet<>();
        }
        Set<String> result = new HashSet<>();
        Arrays.stream(skillText.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.toLowerCase())
                .forEach(result::add);
        return result;
    }

    public static class EligibilityResult {
        private final boolean eligible;
        private final List<String> reasons;
        private final Set<String> missingSkills;

        public EligibilityResult(boolean eligible, List<String> reasons, Set<String> missingSkills) {
            this.eligible = eligible;
            this.reasons = reasons;
            this.missingSkills = missingSkills;
        }

        public boolean isEligible() { return eligible; }
        public List<String> getReasons() { return reasons; }
        public Set<String> getMissingSkills() { return missingSkills; }
    }

    public static class Recommendation {
        private final String jobTitle;
        private final String recommendation;

        public Recommendation(String jobTitle, String recommendation) {
            this.jobTitle = jobTitle;
            this.recommendation = recommendation;
        }

        public String getJobTitle() { return jobTitle; }
        public String getRecommendation() { return recommendation; }
    }
}
