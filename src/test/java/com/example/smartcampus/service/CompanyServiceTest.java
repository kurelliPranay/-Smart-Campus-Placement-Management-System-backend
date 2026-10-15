package com.example.smartcampus.service;

import com.example.smartcampus.entity.ApplicationStatus;
import com.example.smartcampus.entity.CompanyProfile;
import com.example.smartcampus.entity.Job;
import com.example.smartcampus.entity.JobApplication;
import com.example.smartcampus.entity.StudentProfile;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.repository.CompanyProfileRepository;
import com.example.smartcampus.repository.JobApplicationRepository;
import com.example.smartcampus.repository.JobRepository;
import com.example.smartcampus.repository.StudentProfileRepository;
import com.example.smartcampus.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CompanyServiceTest {

    @Test
    void companyApplicantViewIncludesPlacementProfileWithoutReturningUserEntity() {
        CompanyProfileRepository companyRepository = mock(CompanyProfileRepository.class);
        JobRepository jobRepository = mock(JobRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        JobApplicationRepository applicationRepository = mock(JobApplicationRepository.class);
        StudentProfileRepository studentProfileRepository = mock(StudentProfileRepository.class);
        CompanyService service = new CompanyService(
                companyRepository,
                jobRepository,
                userRepository,
                applicationRepository,
                studentProfileRepository
        );

        User companyUser = new User();
        companyUser.setId(1L);
        CompanyProfile company = new CompanyProfile();
        company.setUser(companyUser);

        User student = new User();
        student.setId(2L);
        student.setFullName("Asha Verma");
        student.setEmail("asha@example.edu");

        Job job = new Job();
        job.setId(4L);
        job.setTitle("Java Developer");
        job.setCompany(company);

        JobApplication application = new JobApplication();
        application.setId(3L);
        application.setApplicant(student);
        application.setJob(job);
        application.setStatus(ApplicationStatus.APPLIED);
        application.setAppliedAt("2026-10-10T12:00:00");
        application.setEligible(true);

        StudentProfile profile = new StudentProfile();
        profile.setUser(student);
        profile.setEducation("B.Tech");
        profile.setBranch("CSE");
        profile.setCgpa(8.7);
        profile.setSkills("Java, SQL");
        profile.setProjects("Campus portal");
        profile.setCertifications("Java Fundamentals");
        profile.setResumeUrl("https://example.edu/resume");

        company.setJobs(List.of(job));
        job.setApplications(List.of(application));
        when(companyRepository.findByUser(companyUser)).thenReturn(Optional.of(company));
        when(studentProfileRepository.findByUserIn(anyCollection())).thenReturn(List.of(profile));

        var result = service.getApplicationsForCompany(companyUser);

        assertEquals(1, result.size());
        assertEquals(4L, result.get(0).jobId());
        assertEquals("Asha Verma", result.get(0).applicant().fullName());
        assertEquals("Java Developer", result.get(0).jobTitle());
        assertEquals("CSE", result.get(0).applicant().profile().branch());
        assertEquals(8.7, result.get(0).applicant().profile().cgpa());
        assertEquals("Java, SQL", result.get(0).applicant().profile().skills());
        assertEquals("https://example.edu/resume", result.get(0).applicant().profile().resumeUrl());
        verify(studentProfileRepository).findByUserIn(anyCollection());
    }
}
