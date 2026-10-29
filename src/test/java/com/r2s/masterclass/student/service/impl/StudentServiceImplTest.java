package com.r2s.masterclass.student.service.impl;

import com.r2s.masterclass.shared.exception.StudentAlreadyExistsException;
import com.r2s.masterclass.student.dto.StudentRequestDTO;
import com.r2s.masterclass.student.entity.StudentEntity;
import com.r2s.masterclass.student.repository.StudentRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock
    private StudentRepository repository;

    private StudentServiceImpl service;

    @BeforeEach
    void createService() {
        service = new StudentServiceImpl(repository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"email", "nationalId"})
    void detectsEitherDuplicateBeforeSaving(String field) {
        if (field.equals("email")) {
            when(repository.existsByEmail("student@example.test")).thenReturn(true);
        } else {
            when(repository.existsByNationalId("079123456789")).thenReturn(true);
        }

        assertThatThrownBy(() -> service.register(request()))
                .isInstanceOf(StudentAlreadyExistsException.class)
                .hasMessage("Số CCCD hoặc Email đã tồn tại trên hệ thống OJT");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "uk_student_registration_email", "uk_student_registration_national_id",
            "PUBLIC.UK_STUDENT_REGISTRATION_EMAIL_INDEX_1 ON PUBLIC.STUDENT_REGISTRATIONS(EMAIL)",
            "\"PUBLIC.UK_STUDENT_REGISTRATION_NATIONAL_ID_INDEX_A\" ON PUBLIC.STUDENT_REGISTRATIONS(NATIONAL_ID)"
    })
    void translatesOnlyKnownUniqueConstraintToDuplicate(String constraint) {
        var failure = databaseFailure("23505", constraint);
        when(repository.saveAndFlush(any(StudentEntity.class))).thenThrow(failure);

        assertThatThrownBy(() -> service.register(request()))
                .isInstanceOf(StudentAlreadyExistsException.class)
                .hasMessage("Số CCCD hoặc Email đã tồn tại trên hệ thống OJT");
    }

    @ParameterizedTest
    @MethodSource("unrelatedConstraints")
    void doesNotMisclassifyOtherDatabaseErrors(String sqlState, String constraint) {
        var failure = databaseFailure(sqlState, constraint);
        when(repository.saveAndFlush(any(StudentEntity.class))).thenThrow(failure);

        assertThatThrownBy(() -> service.register(request())).isSameAs(failure);
    }

    static Stream<Arguments> unrelatedConstraints() {
        return Stream.of(
                Arguments.of("23502", "uk_student_registration_email"),
                Arguments.of("22001", "uk_student_registration_national_id"),
                Arguments.of("23505", "PRIMARY_KEY_A"),
                Arguments.of("23505", null),
                Arguments.of("23505", "OTHER_INDEX ON TABLE VALUES('uk_student_registration_email')")
        );
    }

    @Test
    void preservesIntegrityErrorWithoutHibernateConstraintMetadata() {
        var failure = new DataIntegrityViolationException("synthetic database error");
        when(repository.saveAndFlush(any(StudentEntity.class))).thenThrow(failure);

        assertThatThrownBy(() -> service.register(request())).isSameAs(failure);
    }

    private static DataIntegrityViolationException databaseFailure(String sqlState, String constraint) {
        return new DataIntegrityViolationException("synthetic database error",
                new ConstraintViolationException("synthetic constraint", new SQLException("synthetic", sqlState),
                        constraint));
    }

    private static StudentRequestDTO request() {
        return new StudentRequestDTO("Sinh vien mau", "student@example.test", "0901234567",
                "079123456789", "Truong mau", "CNPM");
    }
}
