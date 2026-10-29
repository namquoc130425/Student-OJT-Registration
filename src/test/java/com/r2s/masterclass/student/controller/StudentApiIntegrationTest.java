package com.r2s.masterclass.student.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.r2s.masterclass.student.entity.StudentStatus;
import com.r2s.masterclass.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StudentApiIntegrationTest extends IntegrationTestSupport {

    private static final String BASE = "/api/v1/students";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void registersWithCompleteMaskedResponseAndPersistsOriginalValues() throws Exception {
        JsonNode body = register(payload());
        UUID id = UUID.fromString(body.get("id").asText());

        assertThat(fieldNames(body)).containsExactlyInAnyOrder("id", "fullName", "email", "maskedPhone",
                "maskedNationalId", "university", "major", "status");
        assertThat(body.get("fullName").asText()).isEqualTo("Sinh vien mau");
        assertThat(body.get("maskedPhone").asText()).isEqualTo("090*****67");
        assertThat(body.get("maskedNationalId").asText()).isEqualTo("079******789");
        assertThat(body.get("major").asText()).isEqualTo("CNPM");
        assertThat(body.get("status").asText()).isEqualTo("PENDING_APPROVAL");
        assertThat(body.toString()).doesNotContain("0901234567", "079123456789");
        assertThat(studentRepository.count()).isEqualTo(1);
        var stored = studentRepository.findById(id).orElseThrow();
        assertThat(stored.getPhone()).isEqualTo("0901234567");
        assertThat(stored.getNationalId()).isEqualTo("079123456789");
        assertThat(stored.getStatus()).isEqualTo(StudentStatus.PENDING_APPROVAL);
    }

    @Test
    void lookupReturnsSameCompleteResponseAsRegistration() throws Exception {
        JsonNode created = register(payload());
        var result = mvc.perform(get(BASE + "/" + created.get("id").asText()))
                .andExpect(status().isOk()).andReturn();
        assertThat(body(result)).isEqualTo(created);
    }

    @ParameterizedTest
    @ValueSource(strings = {"email", "nationalId"})
    void duplicateFieldReturns409WithoutAddingAnotherRow(String duplicateField) throws Exception {
        register(payload());
        var next = payload();
        next.put(duplicateField.equals("email") ? "nationalId" : "email",
                duplicateField.equals("email") ? "079123456780" : "other@example.test");

        var result = mvc.perform(post(BASE + "/register").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(next))).andExpect(status().isConflict()).andReturn();

        assertError(body(result), 409, "Số CCCD hoặc Email đã tồn tại trên hệ thống OJT", false);
        assertThat(studentRepository.count()).isEqualTo(1);
    }

    @Test
    void validationErrorsUseFieldObjectAndExactMessages() throws Exception {
        var input = payload();
        input.put("fullName", " ");
        input.put("email", "invalid");
        input.put("phone", "0|12345678");
        input.put("nationalId", "123");
        var result = mvc.perform(post(BASE + "/register").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(input))).andExpect(status().isBadRequest()).andReturn();

        JsonNode response = body(result);
        assertError(response, 400, "Dữ liệu đầu vào không hợp lệ", true);
        assertThat(fieldNames(response.get("errors"))).containsExactlyInAnyOrder(
                "fullName", "email", "phone", "nationalId");
        assertThat(response.get("errors").get("phone").asText())
                .isEqualTo("Số điện thoại phải đủ 10 chữ số hợp lệ tại Việt Nam");
        assertThat(response.get("errors").get("nationalId").asText())
                .isEqualTo("Số CCCD phải đúng 12 chữ số");
        assertThat(response.toString()).doesNotContain("0|12345678");
        assertThat(studentRepository.count()).isZero();
    }

    @ParameterizedTest
    @ValueSource(strings = {"missing", "null", "blank"})
    void missingNullAndBlankRequiredFieldsReturn400(String variant) throws Exception {
        var input = payload();
        List.of("fullName", "email", "phone", "nationalId").forEach(field -> {
            if (variant.equals("missing")) {
                input.remove(field);
            } else {
                input.put(field, variant.equals("null") ? null : "");
            }
        });
        var result = mvc.perform(post(BASE + "/register").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(input))).andExpect(status().isBadRequest()).andReturn();

        JsonNode response = body(result);
        assertError(response, 400, "Dữ liệu đầu vào không hợp lệ", true);
        assertThat(response.get("errors")).isEqualTo(mapper.valueToTree(Map.of(
                "fullName", "Họ và tên không được để trống",
                "email", "Email không được để trống",
                "phone", "Số điện thoại không được để trống",
                "nationalId", "Số CCCD không được để trống")));
        assertThat(studentRepository.count()).isZero();
    }

    @Test
    void optionalFieldsMayBeAbsentButRemainInResponse() throws Exception {
        var input = payload();
        input.remove("university");
        input.remove("major");
        JsonNode response = register(input);
        assertThat(response.has("major")).isTrue();
        assertThat(response.get("major").isNull()).isTrue();
        assertThat(response.has("university")).isTrue();
        assertThat(response.get("university").isNull()).isTrue();
    }

    @Test
    void clientCannotChooseIdOrApprovalStatus() throws Exception {
        var input = payload();
        String clientId = UUID.randomUUID().toString();
        input.put("id", clientId);
        input.put("status", "APPROVED");
        JsonNode response = register(input);

        assertThat(response.get("id").asText()).isNotEqualTo(clientId);
        assertThat(response.get("status").asText()).isEqualTo("PENDING_APPROVAL");
    }

    @Test
    void unknownValidUuidReturns404() throws Exception {
        var result = mvc.perform(get(BASE + "/" + UUID.randomUUID()))
                .andExpect(status().isNotFound()).andReturn();
        assertError(body(result), 404, "Không tìm thấy hồ sơ sinh viên với ID cung cấp", false);
    }

    @ParameterizedTest
    @ValueSource(strings = {"not-a-uuid", "123"})
    void invalidUuidReturns400(String id) throws Exception {
        var result = mvc.perform(get(BASE + "/" + id)).andExpect(status().isBadRequest()).andReturn();
        assertError(body(result), 400, "Dữ liệu đầu vào không hợp lệ", true);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{invalid", "null", "[]", "\"not an object\""})
    void malformedOrWrongShapeJsonReturnsSafe400(String json) throws Exception {
        var result = mvc.perform(post(BASE + "/register").contentType(MediaType.APPLICATION_JSON)
                .content(json)).andExpect(status().isBadRequest()).andReturn();
        assertError(body(result), 400, "Dữ liệu đầu vào không hợp lệ", true);
        assertThat(studentRepository.count()).isZero();
    }

    @Test
    void unsupportedMethodKeeps405InsteadOfBecoming500() throws Exception {
        mvc.perform(put(BASE + "/" + UUID.randomUUID())).andExpect(status().isMethodNotAllowed());
    }

    @Test
    void unsupportedContentTypeKeeps415() throws Exception {
        mvc.perform(post(BASE + "/register").contentType(MediaType.APPLICATION_XML).content("<student/>"))
                .andExpect(status().isUnsupportedMediaType());
    }

    private JsonNode register(Map<String, Object> input) throws Exception {
        return body(mvc.perform(post(BASE + "/register").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(input))).andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andReturn());
    }

    private JsonNode body(MvcResult result) throws Exception {
        return mapper.readTree(result.getResponse().getContentAsByteArray());
    }

    private static Map<String, Object> payload() {
        var input = new LinkedHashMap<String, Object>();
        input.put("fullName", "Sinh vien mau");
        input.put("email", "student@example.test");
        input.put("phone", "0901234567");
        input.put("nationalId", "079123456789");
        input.put("university", "Truong mau");
        input.put("major", "CNPM");
        return input;
    }

    private static List<String> fieldNames(JsonNode node) {
        var names = new ArrayList<String>();
        node.fieldNames().forEachRemaining(names::add);
        return names;
    }

    private static void assertError(JsonNode response, int code, String message, boolean withErrors) {
        assertThat(response.get("status").asInt()).isEqualTo(code);
        assertThat(response.get("message").asText()).isEqualTo(message);
        assertThat(Instant.parse(response.get("timestamp").asText())).isNotNull();
        assertThat(fieldNames(response)).containsExactlyInAnyOrderElementsOf(withErrors
                ? List.of("timestamp", "status", "message", "errors")
                : List.of("timestamp", "status", "message"));
        if (withErrors) {
            assertThat(response.get("errors").isObject()).isTrue();
        }
        assertThat(response.toString()).doesNotContain("stackTrace", "org.hibernate", "org.h2", "SELECT ", "INSERT ");
    }
}
