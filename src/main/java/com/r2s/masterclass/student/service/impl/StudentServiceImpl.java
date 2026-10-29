package com.r2s.masterclass.student.service.impl;

import com.r2s.masterclass.shared.exception.StudentAlreadyExistsException;
import com.r2s.masterclass.shared.exception.StudentNotFoundException;
import com.r2s.masterclass.student.dto.StudentRequestDTO;
import com.r2s.masterclass.student.dto.StudentResponseDTO;
import com.r2s.masterclass.student.entity.StudentEntity;
import com.r2s.masterclass.student.repository.StudentRepository;
import com.r2s.masterclass.student.service.StudentService;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    public StudentServiceImpl(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    @Transactional
    public StudentResponseDTO register(StudentRequestDTO request) {
        if (studentRepository.existsByEmail(request.email())
                || studentRepository.existsByNationalId(request.nationalId())) {
            throw new StudentAlreadyExistsException();
        }

        StudentEntity student = new StudentEntity(request.fullName(), request.email(),
                request.phone(), request.nationalId(), request.university(), request.major());
        try {
            // Flush trong transaction để nhận lỗi unique khi hai request đến đồng thời.
            // https://docs.spring.io/spring-data/jpa/reference/3.5/api/java/org/springframework/data/jpa/repository/JpaRepository.html
            return toResponse(studentRepository.saveAndFlush(student));
        } catch (DataIntegrityViolationException exception) {
            if (isStudentDuplicate(exception)) {
                throw new StudentAlreadyExistsException();
            }
            // Không gắn nhãn lỗi NOT NULL/độ dài/khóa khác thành trùng Email hoặc CCCD.
            throw exception;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponseDTO getById(UUID id) {
        return studentRepository.findById(id)
                .map(StudentServiceImpl::toResponse)
                .orElseThrow(StudentNotFoundException::new);
    }

    private static StudentResponseDTO toResponse(StudentEntity student) {
        return new StudentResponseDTO(student.getId(), student.getFullName(), student.getEmail(),
                student.getPhone(), student.getNationalId(), student.getUniversity(),
                student.getMajor(), student.getStatus().name());
    }

    private static boolean isStudentDuplicate(DataIntegrityViolationException exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException violation
                    && "23505".equals(violation.getSQLState())
                    && violation.getConstraintName() != null) {
                // H2 có thể trả schema, hậu tố index và mô tả sau tên constraint.
                // Chỉ xét identifier đầu tiên, không dò trong giá trị dữ liệu bị từ chối.
                String identifier = violation.getConstraintName().split("\\s+", 2)[0]
                        .replace("\"", "").toLowerCase(Locale.ROOT);
                identifier = identifier.substring(identifier.lastIndexOf('.') + 1);
                return matchesConstraint(identifier, StudentEntity.EMAIL_UNIQUE_CONSTRAINT)
                        || matchesConstraint(identifier, StudentEntity.NATIONAL_ID_UNIQUE_CONSTRAINT);
            }
        }
        return false;
    }

    private static boolean matchesConstraint(String identifier, String constraint) {
        return identifier.equals(constraint) || identifier.startsWith(constraint + "_index_");
    }
}
