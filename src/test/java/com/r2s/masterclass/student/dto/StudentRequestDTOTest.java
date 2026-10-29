package com.r2s.masterclass.student.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class StudentRequestDTOTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void createValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    @ParameterizedTest
    @ValueSource(strings = {"0301234567", "0501234567", "0701234567", "0801234567", "0901234567"})
    void acceptsEachVietnameseMobilePrefix(String phone) {
        var request = new StudentRequestDTO("Sinh vien mau", "student@example.test", phone,
                "079123456789", null, null);
        assertThat(validator.validate(request)).isEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "090123456", "09012345678", "090912345678", "0|12345678",
            "0101234567", "09012345a7", "+84901234567", " 0901234567", "0901234567 ",
            "٠٩٠١٢٣٤٥٦٧", "0901234567\n"})
    void rejectsInvalidPhoneBoundaries(String phone) {
        var request = new StudentRequestDTO("Sinh vien mau", "student@example.test", phone,
                "079123456789", null, null);
        assertThat(validator.validate(request)).isNotEmpty()
                .allSatisfy(error -> assertThat(error.getPropertyPath().toString()).isEqualTo("phone"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"079123456789", "012345678901"})
    void acceptsTwelveDigitNationalIdIncludingLeadingZero(String nationalId) {
        var request = new StudentRequestDTO("Sinh vien mau", "student@example.test", "0901234567",
                nationalId, null, null);
        assertThat(validator.validate(request)).isEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "07912345678", "0791234567890", "07912345678A",
            "079123456789 ", "０７９１２３４５６７８９"})
    void rejectsInvalidNationalIdBoundaries(String nationalId) {
        var request = new StudentRequestDTO("Sinh vien mau", "student@example.test", "0901234567",
                nationalId, null, null);
        assertThat(validator.validate(request)).isNotEmpty()
                .allSatisfy(error -> assertThat(error.getPropertyPath().toString()).isEqualTo("nationalId"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void rejectsMissingOrBlankFullName(String fullName) {
        var request = new StudentRequestDTO(fullName, "student@example.test", "0901234567",
                "079123456789", null, null);
        assertThat(validator.validate(request)).isNotEmpty()
                .allSatisfy(error -> assertThat(error.getPropertyPath().toString()).isEqualTo("fullName"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "invalid", "student@", "@example.test"})
    void rejectsMissingOrInvalidEmail(String email) {
        var request = new StudentRequestDTO("Sinh vien mau", email, "0901234567",
                "079123456789", null, null);
        assertThat(validator.validate(request)).isNotEmpty()
                .allSatisfy(error -> assertThat(error.getPropertyPath().toString()).isEqualTo("email"));
    }

    @Test
    void allowsUniversityAndMajorToBeOmitted() {
        var request = new StudentRequestDTO("Sinh vien mau", "student@example.test", "0901234567",
                "079123456789", null, null);
        assertThat(validator.validate(request)).isEmpty();
    }
}
