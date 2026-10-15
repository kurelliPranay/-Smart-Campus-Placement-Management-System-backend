package com.example.smartcampus.repository;

import com.example.smartcampus.entity.Job;
import com.example.smartcampus.entity.JobApplication;
import com.example.smartcampus.entity.ApplicationStatus;
import com.example.smartcampus.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByApplicant(User applicant);
    List<JobApplication> findByJob(Job job);
    Optional<JobApplication> findByApplicantAndJob(User applicant, Job job);
    long countByJob(Job job);
    long countByStatus(ApplicationStatus status);
}
