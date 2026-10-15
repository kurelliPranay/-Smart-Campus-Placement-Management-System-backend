package com.example.smartcampus.service;

import com.example.smartcampus.entity.Job;
import com.example.smartcampus.entity.StudentProfile;
import com.example.smartcampus.repository.JobApplicationRepository;
import com.example.smartcampus.repository.JobRepository;
import com.example.smartcampus.repository.StudentProfileRepository;
import com.example.smartcampus.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class StudentProfileServiceTest {

    private StudentProfileService service;

    @BeforeEach
    void setUp() {
        service = new StudentProfileService(
                mock(StudentProfileRepository.class),
                mock(UserRepository.class),
                mock(JobRepository.class),
                mock(JobApplicationRepository.class)
        );
    }

    @Test
    void studentMeetsCgpaBranchAndSkillRequirements() {
        StudentProfile student = student("Computer Science", 8.5, "Java, SQL");
        Job job = job(7.0, "Computer Science, Information Technology", "java, sql");

        assertTrue(service.isEligible(student, job).isEligible());
    }

    @Test
    void studentBelowMinimumCgpaIsIneligible() {
        StudentProfile student = student("Computer Science", 6.9, "Java, SQL");
        Job job = job(7.0, "Computer Science", "Java, SQL");

        StudentProfileService.EligibilityResult result = service.isEligible(student, job);

        assertFalse(result.isEligible());
        assertTrue(result.getReasons().contains("CGPA below minimum requirement"));
    }

    @Test
    void studentFromUnlistedBranchIsIneligible() {
        StudentProfile student = student("Mechanical Engineering", 8.5, "Java, SQL");
        Job job = job(7.0, "Computer Science, Information Technology", "Java, SQL");

        StudentProfileService.EligibilityResult result = service.isEligible(student, job);

        assertFalse(result.isEligible());
        assertTrue(result.getReasons().contains("Branch not eligible"));
    }

    @Test
    void missingRequiredSkillsAreReturnedForRecommendation() {
        StudentProfile student = student("Computer Science", 8.5, "Java");
        Job job = job(7.0, "Computer Science", "Java, SQL");

        StudentProfileService.EligibilityResult result = service.isEligible(student, job);

        assertFalse(result.isEligible());
        assertTrue(result.getMissingSkills().contains("sql"));
    }

    @Test
    void applicationDeadlineTodayRemainsOpenThroughTheDay() {
        StudentProfile student = student("Computer Science", 8.5, "Java, SQL");
        Job job = job(7.0, "Computer Science", "Java, SQL");
        job.setApplicationDeadline(LocalDate.now().toString());

        assertTrue(service.isEligible(student, job).isEligible());
    }

    @Test
    void expiredJobDeadlineMakesStudentIneligible() {
        StudentProfile student = student("Computer Science", 8.5, "Java, SQL");
        Job job = job(7.0, "Computer Science", "Java, SQL");
        job.setApplicationDeadline(LocalDate.now().minusDays(1).toString());

        StudentProfileService.EligibilityResult result = service.isEligible(student, job);

        assertFalse(result.isEligible());
        assertTrue(result.getReasons().contains("Application deadline has passed"));
    }

    @Test
    void malformedApplicationDeadlineIsRejected() {
        Job job = job(7.0, "Computer Science", "Java, SQL");
        job.setApplicationDeadline("not-a-date");

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> service.isEligible(student("Computer Science", 8.5, "Java, SQL"), job)
        );
    }

    private StudentProfile student(String branch, double cgpa, String skills) {
        StudentProfile student = new StudentProfile();
        student.setBranch(branch);
        student.setCgpa(cgpa);
        student.setSkills(skills);
        return student;
    }

    private Job job(double minCgpa, String eligibleBranches, String requiredSkills) {
        Job job = new Job();
        job.setMinCgpa(minCgpa);
        job.setEligibleBranches(eligibleBranches);
        job.setRequiredSkills(requiredSkills);
        job.setApplicationDeadline(LocalDate.now().plusDays(1).toString());
        return job;
    }
}
