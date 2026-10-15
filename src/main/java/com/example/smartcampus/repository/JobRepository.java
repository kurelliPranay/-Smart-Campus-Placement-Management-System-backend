package com.example.smartcampus.repository;

import com.example.smartcampus.entity.CompanyProfile;
import com.example.smartcampus.entity.Job;
import com.example.smartcampus.entity.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByStatus(JobStatus status);
    List<Job> findByCompany(CompanyProfile company);
    List<Job> findByCompanyAndStatus(CompanyProfile company, JobStatus status);
}
