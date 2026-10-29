package com.r2s.masterclass.shared.exception;

public class StudentAlreadyExistsException extends RuntimeException {

    public StudentAlreadyExistsException() {
        super("Số CCCD hoặc Email đã tồn tại trên hệ thống OJT");
    }
}
