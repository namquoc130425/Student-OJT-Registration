package com.r2s.masterclass.student.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** ID và trạng thái do server tạo, không thuộc dữ liệu đăng ký. */
public record StudentRequestDTO(
        @NotBlank(message = "Họ và tên không được để trống")
        String fullName,

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        String email,

        @NotBlank(message = "Số điện thoại không được để trống")
        @Pattern(regexp = "^0[35789][0-9]{8}$",
                message = "Số điện thoại phải đủ 10 chữ số hợp lệ tại Việt Nam")
        String phone,

        @NotBlank(message = "Số CCCD không được để trống")
        @Pattern(regexp = "^[0-9]{12}$", message = "Số CCCD phải đúng 12 chữ số")
        String nationalId,

        String university,
        String major
) {
}
