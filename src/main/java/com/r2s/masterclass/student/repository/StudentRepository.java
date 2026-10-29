package com.r2s.masterclass.student.repository;

import com.r2s.masterclass.student.entity.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StudentRepository extends JpaRepository<StudentEntity, UUID> {

    boolean existsByEmail(String email);

    boolean existsByNationalId(String nationalId);
}
