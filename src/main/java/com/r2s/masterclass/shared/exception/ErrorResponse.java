package com.r2s.masterclass.shared.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

/** errors chỉ xuất hiện trong phản hồi 400 có thông tin validation. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        Instant timestamp,
        int status,
        String message,
        Map<String, String> errors
) {

    public ErrorResponse {
        errors = errors == null ? null : Map.copyOf(errors);
    }

    public static ErrorResponse of(int status, String message) {
        return new ErrorResponse(Instant.now(), status, message, null);
    }

    public static ErrorResponse validation(Map<String, String> errors) {
        return new ErrorResponse(Instant.now(), 400, "Dữ liệu đầu vào không hợp lệ", errors);
    }
}
