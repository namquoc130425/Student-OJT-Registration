package com.r2s.masterclass.student.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class StudentResponseDTOTest {

    @ParameterizedTest
    @MethodSource("maskingCases")
    void masksAtConstructionIncludingNullShortAndAlreadyMaskedValues(
            String phone, String nationalId, String expectedPhone, String expectedNationalId) {
        var response = new StudentResponseDTO(UUID.randomUUID(), "Sinh vien mau", "student@example.test",
                phone, nationalId, "Truong mau", "CNPM", "PENDING_APPROVAL");

        assertThat(response.maskedPhone()).isEqualTo(expectedPhone);
        assertThat(response.maskedNationalId()).isEqualTo(expectedNationalId);
        assertThat(response.major()).isEqualTo("CNPM");
        assertThat(response.status()).isEqualTo("PENDING_APPROVAL");
    }

    static Stream<Arguments> maskingCases() {
        return Stream.of(
                Arguments.of("0901234567", "079123456789", "090*****67", "079******789"),
                Arguments.of(null, null, null, null),
                Arguments.of("", "", "", ""),
                Arguments.of("1", "1", "*", "*"),
                Arguments.of("12345", "123456", "*****", "******"),
                Arguments.of("090*****67", "079******789", "090*****67", "079******789")
        );
    }

    @Test
    void recordToStringDoesNotRetainRawPhoneOrNationalId() {
        var response = new StudentResponseDTO(UUID.randomUUID(), "Sinh vien mau", "student@example.test",
                "0901234567", "079123456789", null, null, "PENDING_APPROVAL");

        assertThat(response.toString()).doesNotContain("0901234567", "079123456789")
                .contains("090*****67", "079******789");
    }
}
