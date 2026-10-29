package com.r2s.masterclass.student.service;

import com.r2s.masterclass.student.dto.StudentRequestDTO;
import com.r2s.masterclass.student.dto.StudentResponseDTO;

import java.util.UUID;

public interface StudentService {

    /** Đăng ký hồ sơ; báo lỗi khi Email hoặc CCCD đã tồn tại. */
    StudentResponseDTO register(StudentRequestDTO request);

    /** Tra cứu hồ sơ và báo lỗi nếu UUID không tồn tại. */
    StudentResponseDTO getById(UUID id);
}
