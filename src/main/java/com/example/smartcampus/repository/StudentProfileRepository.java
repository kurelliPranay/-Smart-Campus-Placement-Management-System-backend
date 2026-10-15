package com.example.smartcampus.repository;

import com.example.smartcampus.entity.StudentProfile;
import com.example.smartcampus.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Collection;
import java.util.List;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {
    Optional<StudentProfile> findByUser(User user);
    List<StudentProfile> findByUserIn(Collection<User> users);
}
