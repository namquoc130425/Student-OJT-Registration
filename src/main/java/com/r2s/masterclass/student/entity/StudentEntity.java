package com.r2s.masterclass.student.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.UUID;

/** Mô hình lưu trữ nội bộ, không đưa trực tiếp ra API. */
@Entity
@Table(name = "student_registrations", uniqueConstraints = {
        @UniqueConstraint(name = StudentEntity.EMAIL_UNIQUE_CONSTRAINT, columnNames = "email"),
        @UniqueConstraint(name = StudentEntity.NATIONAL_ID_UNIQUE_CONSTRAINT, columnNames = "national_id")
})
public class StudentEntity {

    public static final String EMAIL_UNIQUE_CONSTRAINT = "uk_student_registration_email";
    public static final String NATIONAL_ID_UNIQUE_CONSTRAINT = "uk_student_registration_national_id";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 10)
    private String phone;

    @Column(name = "national_id", nullable = false, length = 12)
    private String nationalId;

    private String university;

    private String major;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudentStatus status = StudentStatus.PENDING_APPROVAL;

    protected StudentEntity() {
        // Constructor không tham số dành cho JPA.
    }

    public StudentEntity(String fullName, String email, String phone, String nationalId,
                         String university, String major) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.nationalId = nationalId;
        this.university = university;
        this.major = major;
    }

    public UUID getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getNationalId() {
        return nationalId;
    }

    public String getUniversity() {
        return university;
    }

    public String getMajor() {
        return major;
    }

    public StudentStatus getStatus() {
        return status;
    }
}
