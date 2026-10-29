# ADR 001 Tổ chức module sinh viên và ranh giới DTO

## Trạng thái

Đã áp dụng cấu trúc package trong Bước 3 theo phương án người dùng duyệt.
Các lớp nghiệp vụ được triển khai ở Bước 4.

## Ngày

29/10/2026, múi giờ Asia/Saigon.

## Bối cảnh

Bài thực hành yêu cầu hai API đăng ký và tra cứu OJT, giữ logic của đề
và các mẫu tham khảo nhưng tổ chức source, tài liệu theo cách riêng.
Các quyết định đã chốt gồm DTO Record, đầy đủ trường response,
errors object, regex SĐT đúng 10 số và Spring Boot 3.x.

## Quyết định

- Đặt lớp khởi động trong `com.r2s.masterclass`.
- Gom các lớp nghiệp vụ trong `student`, chia controller, dto, service,
  service.impl, entity và repository.
- Đặt định dạng lỗi và handler tập trung trong `shared.exception`.
- Dùng `package-info.java` để ghi trách nhiệm package và giữ khung source
  trong Bước 3, không tạo lớp rỗng hoặc endpoint giả.
- Service interface nhận/trả DTO; Controller không truy cập Entity/Repository.
- DTO không import Entity hoặc enum lưu trữ; status phản hồi là chuỗi.

## Các phương án đã cân nhắc

| Phương án | Đánh giá |
| --- | --- |
| Chia các tầng trực tiếp dưới root | Dễ khởi tạo nhưng không thể hiện ranh giới module sinh viên |
| Module student có các tầng bên trong | Đáp ứng phạm vi nhỏ, đường dẫn dễ theo dõi; được chọn |
| Tách domain, persistence adapter và nhiều module Maven | Tăng số lớp/cấu hình trong khi đề chỉ có hai use case; chưa cần |

## Hệ quả

Spring Boot quét các lớp dưới package khởi động mà không cần cấu hình
scan thủ công. Quy tắc phụ thuộc giữa các tầng phải được giữ khi viết code;
Java không tự cưỡng chế ranh giới chỉ vì có cấu trúc package này.

Masking được thực hiện khi tạo response DTO. Service cung cấp dữ liệu đơn,
giữ Entity ở tầng nội bộ. Hợp đồng HTTP được giữ nguyên dù cách tổ chức
package khác các bản tham khảo.

## Tài liệu

- [Thiết kế chi tiết](../ARCHITECTURE.md).
- [Spring Boot: Structuring Your Code](https://docs.spring.io/spring-boot/3.5/reference/using/structuring-your-code.html).
- [Java 21: Named Packages](https://docs.oracle.com/javase/specs/jls/se21/html/jls-7.html#jls-7.4.1).
