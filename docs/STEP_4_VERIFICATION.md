# Kết quả triển khai Bước 4

**Học viên:** Nguyễn Vương Quốc Nam  
**Ngày kiểm chứng:** 29/10/2026, múi giờ Asia/Saigon  
**Phạm vi:** Triển khai code nghiệp vụ và kiểm tra build/runtime cơ bản.

## Mã nguồn đã triển khai

- Entity UUID với trạng thái mặc định `PENDING_APPROVAL`, unique constraint
  riêng cho Email và CCCD; Repository có hai phương thức kiểm tra trùng.
- Request Record với Jakarta Validation; không tự thêm quy tắc bắt buộc
  cho university hoặc major.
- Response Record che SĐT/CCCD trong constructor, không phụ thuộc Entity,
  có đủ tám trường cho cả POST và GET.
- Service interface và implementation dùng constructor injection,
  transaction ghi cho đăng ký, transaction chỉ đọc cho tra cứu.
- Hai endpoint đúng đề; Controller không truy cập Entity/Repository.
- Exception handler cho lỗi nghiệp vụ và lỗi request, đúng thông báo và
  cấu trúc JSON đã chốt.

## Kiểm chứng build

Đã chạy bằng JDK 21.0.12:

```powershell
.\mvnw.cmd -o -B -ntp verify
```

Log chính:

```text
Compiling 20 source files with javac [debug parameters release 21]
No tests to run.
BUILD SUCCESS
```

Chưa có bộ test JUnit tại bước này. `BUILD SUCCESS` xác nhận biên dịch và
đóng gói thành công, không có nghĩa tất cả kịch bản kiểm thử đã đạt.

## Kiểm chứng runtime đã thực hiện

Chạy file JAR với JDK 21 và cấu hình socket local đã xác minh ở Bước 2:

```powershell
java '-Djdk.net.unixdomain.tmpdir=target' -jar 'target/student-ojt-registration-0.0.1-SNAPSHOT.jar'
```

Ứng dụng khởi động tại `127.0.0.1:8080`, quét được một Repository, tạo bảng
`student_registrations` và hai unique constraint có tên rõ ràng.

Dùng HTTP client với dữ liệu giả lập để gửi tám request kiểm tra dưới đây.

| Tình huống đã chạy | Kết quả thực tế |
| --- | --- |
| Đăng ký hợp lệ | 201, UUID hợp lệ, đủ tám trường, major và PENDING_APPROVAL |
| GET UUID vừa đăng ký | 200, response bằng response POST và đã masking |
| Trùng Email nhưng CCCD khác | 409, đúng message; chỉ timestamp/status/message |
| Trùng CCCD nhưng Email khác | 409, đúng message; chỉ timestamp/status/message |
| Họ tên trống, Email/SĐT/CCCD sai | 400, errors object có đủ bốn trường |
| UUID hợp lệ không tồn tại | 404, đúng message và cấu trúc |
| UUID sai định dạng | 400, thông báo chung; không trả giá trị exception |
| JSON sai cú pháp | 400, không trả chi tiết lỗi Jackson |

Trong phản hồi thành công đã kiểm tra:

- `0901234567` được che thành `090*****67`.
- `079123456789` được che thành `079******789`.
- Không có trường phone/nationalId gốc hoặc số nguyên bản trong JSON.

Trong phản hồi lỗi đã kiểm tra đúng key theo contract; không có trace,
exception, SQL hoặc path. Thông báo SĐT và CCCD khớp nguyên văn trong đề.

## Rà soát code

| Trục rà soát | Kết quả |
| --- | --- |
| Đúng yêu cầu | Hai API, DTO Record, đủ major/status, regex và thông báo theo đề |
| Dễ đọc | Lớp theo trách nhiệm, constructor injection, không thêm mapper framework |
| Kiến trúc | DTO không import Entity; service interface không nhận kiểu HTTP |
| Bảo vệ dữ liệu | Validation ở biên, response tự masking, lỗi request không dùng exception message thô |
| Truy cập dữ liệu | Query Spring Data, không ghép SQL từ input; không có quan hệ JPA gây N+1 |

Đã dùng `saveAndFlush` và chỉ chuyển vi phạm hai unique constraint của hồ
sơ thành lỗi nghiệp vụ trùng. Lỗi NOT NULL, độ dài hoặc constraint khác
không bị gán thành 409. Nhánh xử lý lỗi unique khi có request đồng thời đã
có code; việc kích hoạt và kiểm chứng nhánh này thuộc Bước 5.

Không thêm endpoint, cơ chế đăng nhập, dependency hoặc nghiệp vụ ngoài đề.
Không có generic handler bắt mọi Exception rồi biến lỗi Spring MVC thành 500.

## Phần còn lại cho Bước 5

- Bộ test JUnit/Mockito và integration test với database thật trong bộ nhớ.
- Thiếu/null/chuỗi trống, độ dài và đầu số SĐT, định dạng CCCD/Email.
- Các đường tạo response và trường hợp masking biên.
- Kiểm tra ràng buộc DB và đăng ký trùng đồng thời.
- Kiểm tra lỗi kỹ thuật không bị nhầm thành 409 và không lộ dữ liệu nội bộ.
- Kiểm thử hồi quy, lệnh cURL nghiệm thu và audit checklist bằng bằng chứng.

Chưa ghi nhận Quality Gate tổng thể là 100% PASS.

## Nguồn kỹ thuật

- [Spring MVC 6.2: Validation](https://docs.spring.io/spring-framework/reference/6.2/web/webmvc/mvc-controller/ann-validation.html).
- [Spring MVC 6.2: ResponseEntityExceptionHandler](https://docs.spring.io/spring-framework/docs/6.2.x/javadoc-api/org/springframework/web/servlet/mvc/method/annotation/ResponseEntityExceptionHandler.html).
- [Spring Data JPA 3.5: saveAndFlush](https://docs.spring.io/spring-data/jpa/reference/3.5/api/java/org/springframework/data/jpa/repository/JpaRepository.html#saveAndFlush(S)).
- [Hibernate 6.6: ConstraintViolationException](https://docs.hibernate.org/orm/6.6/javadocs/org/hibernate/exception/ConstraintViolationException.html).
