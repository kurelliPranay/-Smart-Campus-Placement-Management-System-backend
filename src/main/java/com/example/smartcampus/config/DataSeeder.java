package com.example.smartcampus.config;

import com.example.smartcampus.entity.CompanyProfile;
import com.example.smartcampus.entity.Job;
import com.example.smartcampus.entity.JobStatus;
import com.example.smartcampus.entity.StudentProfile;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.entity.UserRole;
import com.example.smartcampus.repository.CompanyProfileRepository;
import com.example.smartcampus.repository.JobRepository;
import com.example.smartcampus.repository.StudentProfileRepository;
import com.example.smartcampus.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CompanyProfileRepository companyProfileRepository;
    private final JobRepository jobRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.name}")
    private String adminName;

    public DataSeeder(UserRepository userRepository,
                      CompanyProfileRepository companyProfileRepository,
                      JobRepository jobRepository,
                      StudentProfileRepository studentProfileRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.companyProfileRepository = companyProfileRepository;
        this.jobRepository = jobRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setFullName(adminName);
            admin.setRole(UserRole.ADMIN);
            admin.setEnabled(true);
            userRepository.save(admin);
        }

        if (companyProfileRepository.count() == 0) {
            User companyUser = userRepository.findByEmail("info@techspark.com").orElse(null);
            if (companyUser == null) {
                companyUser = new User();
                companyUser.setEmail("info@techspark.com");
                companyUser.setPassword(passwordEncoder.encode("Company@123"));
                companyUser.setFullName("TechSpark Solutions");
                companyUser.setRole(UserRole.COMPANY);
                companyUser.setEnabled(true);
                companyUser = userRepository.save(companyUser);
            }

            CompanyProfile company = new CompanyProfile();
            company.setUser(companyUser);
            company.setCompanyName("TechSpark Solutions");
            company.setIndustry("Software Services");
            company.setDescription("Modern software engineering company");
            company.setWebsite("https://example.com");
            company.setContactEmail("info@techspark.com");
            companyProfileRepository.save(company);

            if (jobRepository.count() == 0) {
                Job job1 = new Job();
                job1.setCompany(company);
                job1.setTitle("Java Developer Intern");
                job1.setDescription("Build backend systems and microservices.");
                job1.setRequiredSkills("Java, Spring Boot, SQL");
                job1.setEligibleBranches("CSE, IT, ECE");
                job1.setMinCgpa(7.5);
                job1.setApplicationDeadline("2026-12-31");
                job1.setStatus(JobStatus.PUBLISHED);
                jobRepository.save(job1);

                Job job2 = new Job();
                job2.setCompany(company);
                job2.setTitle("Frontend Engineer");
                job2.setDescription("Create responsive web interfaces.");
                job2.setRequiredSkills("React, JavaScript, HTML");
                job2.setEligibleBranches("CSE, IT");
                job2.setMinCgpa(7.0);
                job2.setApplicationDeadline("2026-11-30");
                job2.setStatus(JobStatus.PUBLISHED);
                jobRepository.save(job2);
            }
        }

        if (studentProfileRepository.count() == 0) {
            User student = userRepository.findByEmail("student@college.edu").orElse(null);
            if (student == null) {
                student = new User();
                student.setEmail("student@college.edu");
                student.setPassword(passwordEncoder.encode("Student@123"));
                student.setFullName("Asha Verma");
                student.setRole(UserRole.STUDENT);
                student.setEnabled(true);
                student = userRepository.save(student);
            }

            StudentProfile profile = new StudentProfile();
            profile.setUser(student);
            profile.setBranch("CSE");
            profile.setCgpa(8.7);
            profile.setEducation("B.Tech in Computer Science");
            profile.setSkills("Java, HTML, CSS, SQL");
            profile.setProjects("Built campus portal and inventory app");
            profile.setCertifications("Java Fundamentals, SQL Workshop");
            profile.setResumeUrl("https://example.com/resume");
            studentProfileRepository.save(profile);
        }
    }
}
