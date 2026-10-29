package com.r2s.masterclass.student.dto;

import java.util.UUID;

/** Constructor luôn che PII để mọi response, kể cả toString, không chứa số gốc. */
public record StudentResponseDTO(
        UUID id,
        String fullName,
        String email,
        String maskedPhone,
        String maskedNationalId,
        String university,
        String major,
        String status
) {

    public StudentResponseDTO {
        maskedPhone = mask(maskedPhone, 3, 2);
        maskedNationalId = mask(maskedNationalId, 3, 3);
    }

    private static String mask(String value, int visiblePrefix, int visibleSuffix) {
        if (value == null) {
            return null;
        }
        if (value.length() <= visiblePrefix + visibleSuffix) {
            return "*".repeat(value.length());
        }
        return value.substring(0, visiblePrefix)
                + "*".repeat(value.length() - visiblePrefix - visibleSuffix)
                + value.substring(value.length() - visibleSuffix);
    }
}
