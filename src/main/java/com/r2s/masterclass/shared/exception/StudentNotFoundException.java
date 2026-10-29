package com.r2s.masterclass.shared.exception;

public class StudentNotFoundException extends RuntimeException {

    public StudentNotFoundException() {
        super("Không tìm thấy hồ sơ sinh viên với ID cung cấp");
    }
}
