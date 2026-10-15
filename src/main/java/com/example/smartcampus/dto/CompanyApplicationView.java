package com.example.smartcampus.dto;

import com.example.smartcampus.entity.ApplicationStatus;

public record CompanyApplicationView(
        Long id,
        Long jobId,
        ApplicantView applicant,
        String jobTitle,
        ApplicationStatus status,
        String appliedAt,
        boolean eligible
) {
    public record ApplicantView(
            String fullName,
            String email,
            StudentProfileView profile
    ) {}

    public record StudentProfileView(
            String education,
            String branch,
            double cgpa,
            String skills,
            String projects,
            String certifications,
            String resumeUrl
    ) {}
}
