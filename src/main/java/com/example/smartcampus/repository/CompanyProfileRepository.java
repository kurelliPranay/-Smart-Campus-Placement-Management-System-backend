package com.example.smartcampus.repository;

import com.example.smartcampus.entity.CompanyProfile;
import com.example.smartcampus.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyProfileRepository extends JpaRepository<CompanyProfile, Long> {
    Optional<CompanyProfile> findByUser(User user);
}
