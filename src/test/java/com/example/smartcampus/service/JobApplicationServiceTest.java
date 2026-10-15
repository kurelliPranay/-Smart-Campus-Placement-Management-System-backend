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
import com.example.smartcampus.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JobApplicationServiceTest {

    private JobApplicationRepository applicationRepository;
    private JobRepository jobRepository;
    private StudentProfileRepository profileRepository;
    private JobApplicationService service;
    private User student;
    private Job job;
    private StudentProfile profile;

    @BeforeEach
    void setUp() {
        applicationRepository = mock(JobApplicationRepository.class);
        jobRepository = mock(JobRepository.class);
        profileRepository = mock(StudentProfileRepository.class);
        StudentProfileService profileService = new StudentProfileService(
                profileRepository,
                mock(UserRepository.class),
                jobRepository,
                applicationRepository
        );
        service = new JobApplicationService(applicationRepository, jobRepository, profileRepository, profileService);

        student = new User();
        student.setId(12L);
        student.setRole(UserRole.STUDENT);

        job = new Job();
        job.setId(34L);
        job.setStatus(JobStatus.PUBLISHED);
        job.setMinCgpa(0);
        job.setApplicationDeadline(LocalDate.now().plusDays(1).toString());

        profile = new StudentProfile();
        profile.setBranch("Computer Science");
        profile.setCgpa(8.0);
        profile.setSkills("Java, SQL");
        when(jobRepository.findById(34L)).thenReturn(Optional.of(job));
        when(profileRepository.findByUser(student)).thenReturn(Optional.of(profile));
        when(applicationRepository.findByApplicantAndJob(student, job)).thenReturn(Optional.empty());
    }

    @Test
    void eligibleStudentCanApplyToPublishedJob() {
        when(applicationRepository.save(any(JobApplication.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        JobApplication application = service.applyForJob(student, 34L);

        assertEquals(student, application.getApplicant());
        assertEquals(job, application.getJob());
        assertEquals(ApplicationStatus.APPLIED, application.getStatus());
        assertNotNull(application.getAppliedAt());
        verify(applicationRepository).save(application);
    }

    @Test
    void nonStudentCannotApply() {
        User company = new User();
        company.setRole(UserRole.COMPANY);

        assertThrows(IllegalArgumentException.class, () -> service.applyForJob(company, 34L));

        verify(jobRepository, never()).findById(34L);
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void studentCannotApplyToUnpublishedJob() {
        job.setStatus(JobStatus.DRAFT);

        assertThrows(IllegalArgumentException.class, () -> service.applyForJob(student, 34L));

        verify(profileRepository, never()).findByUser(student);
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void studentCannotApplyAfterJobDeadline() {
        job.setApplicationDeadline(LocalDate.now().minusDays(1).toString());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.applyForJob(student, 34L)
        );

        assertEquals("The application deadline for this job has passed", exception.getMessage());
        verify(profileRepository, never()).findByUser(student);
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void studentMustHaveProfileBeforeApplying() {
        when(profileRepository.findByUser(student)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.applyForJob(student, 34L));

        verify(applicationRepository, never()).save(any());
    }

    @Test
    void duplicateApplicationIsRejected() {
        when(applicationRepository.findByApplicantAndJob(student, job))
                .thenReturn(Optional.of(new JobApplication()));

        assertThrows(IllegalArgumentException.class, () -> service.applyForJob(student, 34L));

        verify(applicationRepository, never()).save(any());
    }

    @Test
    void ineligibleStudentCannotApply() {
        job.setEligibleBranches("Electrical Engineering");

        assertThrows(IllegalArgumentException.class, () -> service.applyForJob(student, 34L));

        verify(applicationRepository, never()).save(any());
    }
}
