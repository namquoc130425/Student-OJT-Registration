# DEV PROMPT LIBRARY

Dự án: Student OJT Registration Service  
Học viên: Nguyễn Vương Quốc Nam  
Lớp: 6H Masterclass — AI Agent & Code Audit

## 1. Cấu hình lá chắn vùng đỏ

Tạo `.copilotignore` hoặc `.cursorignore` tại thư mục gốc,
phù hợp với công cụ sử dụng, gồm:

```gitignore
src/main/resources/application*.yml
src/main/resources/application*.yaml
src/main/resources/application*.properties
.env
.env.*
*.pem
*.key
secrets/
```

`.gitignore` được cấu hình riêng để loại trừ file khỏi Git.
Không sử dụng `.gitignore` làm bằng chứng chặn công cụ AI đọc file.
Hiệu lực loại trừ của công cụ AI phải được kiểm tra thực tế.

Không đưa mật khẩu, khóa truy cập hoặc dữ liệu cá nhân thật
vào prompt, tài liệu và dữ liệu kiểm thử.

## 2. Prompt phát triển theo R-C-T-C-O

```text
[ROLE]

Bạn là Senior Java Architect, chịu trách nhiệm xây dựng
REST API Spring Boot đúng đặc tả, phân lớp rõ ràng và bảo vệ
dữ liệu cá nhân.

[CONTEXT]

Xây dựng phân hệ đăng ký sinh viên OJT theo bài tập
R2S Academy, sử dụng:

- Java 21.
- Spring Boot 3.x, đáp ứng yêu cầu 3.2+ của đề.
- Maven.
- Spring Web.
- Spring Data JPA.
- Jakarta Validation.
- H2 In-Memory Database.
- Java Record cho request DTO và response DTO.
- Không sử dụng Lombok.

Dự án có hai API:

1. POST /api/v1/students/register
2. GET /api/v1/students/{id}

Mã nguồn được tự triển khai theo đặc tả. Các dự án tham khảo
được dùng để đối chiếu nghiệp vụ.

[TASK]

Triển khai các thành phần:

1. StudentRequestDTO:
   Nhận fullName, email, phone, nationalId, university, major;
   thực hiện Jakarta Validation.

2. StudentResponseDTO:
   Trả id, fullName, email, maskedPhone, maskedNationalId,
   university, major, status.

3. StudentEntity:
   Lưu hồ sơ với UUID và trạng thái mặc định PENDING_APPROVAL.

4. StudentRepository:
   Truy cập dữ liệu và cung cấp existsByEmail,
   existsByNationalId.

5. StudentService và StudentServiceImpl:
   Xử lý đăng ký, kiểm tra trùng và tra cứu hồ sơ theo UUID.

6. StudentController:
   Nhận request DTO, gọi Service và trả response DTO
   với HTTP status đúng API Contract.

7. GlobalExceptionHandler và các exception cần thiết:
   Xử lý validation 400, duplicate 409 và not found 404.

[CONSTRAINT]

A. Giới hạn nghiệp vụ

- Chỉ triển khai hai API theo đề.
- Không tự bổ sung nghiệp vụ ngoài đặc tả.
- POST thành công trả 201 Created.
- GET thành công trả 200 OK.
- Cả hai API thành công đều trả đủ major và status.
- Không nhận id hoặc status từ request đăng ký.

B. Kiến trúc

- Controller chỉ nhận và trả DTO dạng Java Record.
- Không trả StudentEntity trực tiếp qua API.
- Service xử lý nghiệp vụ; Repository truy cập database.
- Sử dụng constructor injection.
- Không sử dụng getter/setter hoặc builder thay cho Record DTO.

C. Validation

- fullName không được để trống.
- email phải đúng định dạng email.
- phone là số di động Việt Nam gồm đúng 10 chữ số.
- Regex phone: ^0[35789][0-9]{8}$
- nationalId gồm đúng 12 chữ số.
- Regex nationalId: ^[0-9]{12}$
- Các trường bắt buộc không được lọt validation khi null.
- Không tự đặt thêm quy tắc bắt buộc cho university và major
  khi chưa có yêu cầu trong đặc tả.

Thông báo lỗi theo API Contract:

- phone: "Số điện thoại phải đủ 10 chữ số hợp lệ tại Việt Nam"
- nationalId: "Số CCCD phải đúng 12 chữ số"
- Các thông báo validation khác sử dụng tiếng Việt.

D. Masking PII

- Masking tự động khi tạo response DTO, trước khi trả client.
- 0901234567 phải trở thành 090*****67.
- 079123456789 phải trở thành 079******789.
- Response không chứa phone hoặc nationalId nguyên bản.
- Áp dụng cho cả POST và GET.
- Masking là che thông tin hiển thị, không gọi là mã hóa dữ liệu.

E. Kiểm tra trùng

- Kiểm tra trùng Email và CCCD trong Service.
- Ràng buộc unique tại database cho cả hai trường.
- Xử lý trường hợp đăng ký trùng đồng thời.
- Trả 409 với message:
  "Số CCCD hoặc Email đã tồn tại trên hệ thống OJT"

F. Xử lý lỗi

Validation trả JSON gồm:

{
  "timestamp": "...",
  "status": 400,
  "message": "Dữ liệu đầu vào không hợp lệ",
  "errors": {
    "field": "message"
  }
}

Không tìm thấy hồ sơ trả JSON gồm:

{
  "timestamp": "...",
  "status": 404,
  "message": "Không tìm thấy hồ sơ sinh viên với ID cung cấp"
}

Trùng Email hoặc CCCD trả JSON gồm:

{
  "timestamp": "...",
  "status": 409,
  "message": "Số CCCD hoặc Email đã tồn tại trên hệ thống OJT"
}

- Không trả SQL, stacktrace hoặc thông tin kết nối database.
- Không tự đổi tên errors thành fieldErrors.
- Không tự thêm trường làm thay đổi API Contract.

G. Quy trình thực hiện

- Thực hiện theo 5 bước đã thống nhất với người dùng.
- Trình bày nội dung từng bước để duyệt trước khi thực hiện.
- Không tự chuyển sang bước tiếp theo.
- Chỉ ghi nhận kết quả build, test và audit đã thực sự chạy.

[OUTPUT]

- Source Java có package và đường dẫn file rõ ràng.
- Comment tiếng Việt ngắn gọn tại những phần cần giải thích.
- Cấu hình Maven và hướng dẫn kết nối H2.
- README hướng dẫn chạy và kiểm thử trên Windows.
- Bộ kiểm thử nghiệp vụ, API Contract và masking.
- Audit checklist kèm bằng chứng kiểm chứng thực tế.
```

### Prompt triển khai Bước 2

```text
[ROLE]
Bạn là Java Build Engineer phụ trách khởi tạo dự án Spring Boot.

[CONTEXT]
Học viên Nguyễn Vương Quốc Nam đã duyệt Bước 2. Dùng Java 21,
Spring Boot 3.5.16, Maven 3.9.15, H2 in-memory, JPA và Validation.
Mọi ràng buộc nghiệp vụ trong prompt chính tiếp tục có hiệu lực.

[TASK]
Tạo pom.xml, Maven Wrapper, lớp khởi động, cấu hình mẫu H2 và README.
Tạo cấu hình local từ mẫu rồi build và chạy để kiểm chứng kết nối H2.

[CONSTRAINT]
Chỉ thực hiện Bước 2. Không tạo các lớp hoặc API nghiệp vụ của Bước 3/4.
Dùng spring-boot-starter-parent để quản lý dependency. Không dùng Lombok.
Dùng JDK 21 trong terminal, không thay đổi JAVA_HOME toàn máy.
Cấu hình mẫu chỉ dùng database thực hành, không chứa bí mật thật.
Không ghi nhận test nghiệp vụ đã PASS khi chưa triển khai API.

[OUTPUT]
Khung dự án build được, Maven Wrapper chạy bằng JDK 21,
ứng dụng khởi động và truy vấn được H2; báo cáo kết quả thực tế.
```

### Prompt triển khai Bước 3

```text
[ROLE]
Bạn là Java Architect phụ trách tổ chức source và hợp đồng giữa các lớp.

[CONTEXT]
Học viên Nguyễn Vương Quốc Nam đã duyệt Bước 3. Khung Spring Boot 3.5.16,
Java 21, Maven Wrapper và H2 đã được kiểm chứng tại Bước 2.
Giữ nguyên prompt chính và sáu quyết định đã được người dùng chốt.

[TASK]
Tạo khung package student/controller, dto, service, service/impl,
entity, repository và shared/exception bằng package-info.java.
Viết tài liệu danh mục lớp, hợp đồng DTO/service/repository,
hai luồng nghiệp vụ và định dạng lỗi cho Bước 4.

[CONSTRAINT]
Chỉ làm kiến trúc Bước 3; không tạo Entity, DTO, service hay endpoint giả.
Không triển khai nghiệp vụ trước khi người dùng duyệt Bước 4.
Controller chỉ nhận/trả DTO Record và gọi service interface.
DTO không phụ thuộc Entity. Không thêm dependency hoặc cấu hình scan thừa.
Giữ lớp khởi động ở package cha và kiểm tra dự án vẫn build được.

[OUTPUT]
Khung package có mô tả trách nhiệm, docs/ARCHITECTURE.md,
quyết định kiến trúc và báo cáo kiểm chứng Bước 3.
```

### Prompt triển khai Bước 4

```text
[ROLE]
Bạn là Senior Java Developer triển khai API đúng hợp đồng và kiến trúc đã duyệt.

[CONTEXT]
Nguyễn Vương Quốc Nam đã duyệt Bước 4. Dự án dùng Spring Boot 3.5.16,
Java 21, JPA, Jakarta Validation, H2; xem docs/ARCHITECTURE.md.

[TASK]
Viết Entity/Repository, DTO Record, Service, hai API đăng ký/tra cứu và
exception handler; cập nhật README và kiểm chứng build/runtime cơ bản.

[CONSTRAINT]
Giữ nguyên sáu quyết định và thông báo lỗi trong prompt chính.
DTO không phụ thuộc Entity, tự masking trước khi phản hồi.
Email/CCCD phải được bảo vệ bởi kiểm tra service và unique constraint DB.
Không biến mọi lỗi DataIntegrityViolationException thành 409.
Không thêm nghiệp vụ, dependency, Lombok hoặc thay đổi cấu hình bí mật.
Bước 5 vẫn là bước kiểm thử nghiệm thu tổng thể và audit checklist.

[OUTPUT]
Mã nguồn hoạt động, build thành công và báo cáo Bước 4 ghi đúng phạm vi
đã kiểm chứng, không đánh dấu toàn bộ test nghiệp vụ là PASS.
```

### Prompt triển khai Bước 5

```text
[ROLE]
Bạn là Java QA Engineer kiểm chứng code theo API Contract, không suy đoán PASS.

[CONTEXT]
Nguyễn Vương Quốc Nam đã duyệt Bước 5. Hai API được triển khai ở Bước 4,
Java 21, Spring Boot 3.5.16, JPA, H2, Jakarta Validation và DTO Record.

[TASK]
Viết và chạy unit/integration test, giá trị biên, chống trùng đồng thời,
kiểm tra rò rỉ dữ liệu trong response; chạy cURL và hoàn thiện audit checklist.

[CONSTRAINT]
Kiểm tra đầu ra và dữ liệu thực tế; không mock toàn bộ luồng tích hợp.
DB test dùng H2 riêng và được dọn giữa các test. Test đồng thời có điểm
đồng bộ và timeout; phải chứng minh nhánh xung đột unique ở DB được chạy.
Nếu phát hiện lỗi, giữ test tái hiện trước khi sửa, rồi chạy hồi quy.
Không thay đổi nghiệp vụ đã duyệt hoặc ghi nhận GitHub/AI guardrail đã
kiểm chứng khi chưa có bằng chứng. Không push hoặc gửi dữ liệu ra ngoài.

[OUTPUT]
Bộ test chạy lại được, script cURL nghiệm thu, báo cáo Bước 5,
AUDIT_CHECKLIST.md và nhật ký audit với số liệu thực tế.
```

## 3. Nhật ký audit và điều chỉnh

Trạng thái hiện tại: Đã hoàn thành năm bước tại local, chạy toàn bộ 80 test
tự động và 10 kịch bản cURL thành công. Bằng chứng GitHub và hiệu lực chặn
đọc thực tế của công cụ AI chưa xác minh; không tuyên bố toàn bộ DoD là 100% PASS.

### Audit Bước 2

- Hạng mục: Phiên bản Java dùng để build và chạy.
- Hiện tượng: `JAVA_HOME` của máy trỏ tới JDK 24, khác Java 21 đã duyệt.
- Đối chiếu: Dự án phải sử dụng Java 21.
- Điều chỉnh: Chọn JDK 21 trong terminal kiểm chứng; không đổi toàn máy.
- Kiểm chứng: `mvnw.cmd -version`, `mvnw.cmd -B -ntp clean verify`.
- Kết quả: Maven 3.9.15 dùng Java 21.0.12, biên dịch `release 21`, BUILD SUCCESS.

- Hạng mục: Khởi động HTTP server trên môi trường Windows kiểm chứng.
- Hiện tượng: Java NIO không tạo được kết nối socket nội bộ với thư mục mặc định.
- Điều chỉnh: Chỉ định `-Djdk.net.unixdomain.tmpdir=target` cho tiến trình chạy.
- Kiểm chứng: Java Selector tối thiểu, chạy JAR và chạy qua Maven.
- Kết quả: Tomcat khởi động cổng 8080; H2 Console HTTP 200;
  truy vấn SELECT 1 trên database hiện có trả 1, catalog OJTDB.

Bằng chứng chi tiết: `docs/STEP_2_VERIFICATION.md`.
Maven báo `No tests to run`; chưa đánh dấu các tiêu chí nghiệp vụ là PASS.

### Audit Bước 3

- Hạng mục: Package và trách nhiệm các tầng.
- Thực hiện: Tạo bảy package-info.java, giữ lớp khởi động tại package cha.
- Đối chiếu: Controller dùng DTO/service; DTO không phụ thuộc Entity;
  response giữ đủ major, status và các trường đã chốt.
- Kiểm chứng: Kiểm tra tên package khớp đường dẫn, giới hạn source,
  UTF-8, code fence và liên kết tài liệu; chạy `mvnw.cmd -o -B -ntp verify`.
- Kết quả: Tám file Java biên dịch với release 21, BUILD SUCCESS.
- Giới hạn: Chưa có code nghiệp vụ; Maven báo No tests to run.

Bằng chứng chi tiết: `docs/STEP_3_VERIFICATION.md`.

### Audit Bước 4

- Hạng mục: DTO và masking.
- Thực hiện: Cả request/response là Record; response tự che PII trong
  constructor; Service chuyển các giá trị đơn, DTO không nhận Entity.
- Kiểm chứng: POST 201 và GET 200 có cùng tám trường, đủ major/status,
  SĐT và CCCD đúng mẫu che thông tin; không có số gốc trong JSON.

- Hạng mục: Validation và xử lý lỗi.
- Thực hiện: Ưu tiên thông báo NotBlank khi một trường có nhiều lỗi;
  errors là object. Kế thừa ResponseEntityExceptionHandler để giữ status MVC.
- Kiểm chứng: Trùng riêng từng trường trả 409; validation 400; UUID không
  tồn tại 404; UUID sai và JSON lỗi 400. Message và key được đối chiếu với đề.

- Hạng mục: Chống trùng tại database.
- Thực hiện: Unique constraint Email/CCCD, saveAndFlush trong transaction,
  chỉ ánh xạ đúng hai unique constraint này thành StudentAlreadyExistsException.
- Giới hạn: Kiểm thử đồng thời và test nhánh DB thực hiện ở Bước 5.

- Kết quả build: `mvnw.cmd -o -B -ntp verify` trả BUILD SUCCESS,
  20 file Java biên dịch với release 21; Maven báo No tests to run.

Bằng chứng chi tiết: `docs/STEP_4_VERIFICATION.md`.
Chưa đánh dấu audit tổng thể hoặc toàn bộ nghiệp vụ là 100% PASS.

### Audit Bước 5

- Hạng mục: Bộ test tự động và database.
- Thực hiện: 40 test request DTO, 7 test response DTO, 19 test API,
  12 test Service và 2 test đồng thời trên H2 thật.
- Kiểm chứng: `mvnw.cmd -o -B -ntp clean verify` chạy cả suite.
- Kết quả: 80 test, 0 failure, 0 error, 0 skipped; BUILD SUCCESS.

- Hạng mục: Tính đúng của unique constraint khi cạnh tranh.
- Thực hiện: Barrier bảo đảm hai transaction vượt precheck trước khi ghi;
  exists/save chuyển tiếp vào Repository thật, không giả lập kết quả DB.
- Kết quả: Mỗi trường hợp trùng Email/CCCD chỉ commit một hồ sơ,
  có đúng một lỗi unique từ H2 và được chuyển thành exception trùng đúng message.

- Hạng mục: Cấu hình chạy test trên Java 21.
- Hiện tượng: Lần test tích hợp đầu dừng lâu sau bước Mockito tự attach agent.
- Điều chỉnh: Nạp Mockito bằng javaagent của Surefire, đặt thư mục socket
  test tại target và timeout tiến trình. Không đổi JDK/cấu hình toàn máy.
- Kết quả: Chạy lại đạt; lần bị dừng không được tính là PASS.

- Hạng mục: Nghiệm thu HTTP và che lỗi nội bộ.
- Kiểm chứng: `scripts/Test-Api.ps1` thực thi curl.exe với dữ liệu giả lập.
- Kết quả: 10/10 check đạt, gồm lỗi DB 500 không bị đổi thành 409 và không
  lộ SQL/trace/CCCD qua HTTP. Server được dừng sau khi kiểm tra.

Bằng chứng: `docs/STEP_5_VERIFICATION.md`, `docs/test-results.json`,
`AUDIT_CHECKLIST.md` và báo cáo JUnit sinh tại `target/surefire-reports/`.
Không phát hiện lỗi nghiệp vụ cần sửa trong các kịch bản đã chạy.

### Mẫu ghi nhận khi có thay đổi sau nghiệm thu

Mỗi phát hiện được ghi theo mẫu:

- Hạng mục:
- Hiện tượng hoặc lỗi phát hiện:
- Đối chiếu yêu cầu trong đề:
- Điều chỉnh đã thực hiện:
- Lệnh hoặc kịch bản kiểm chứng:
- Kết quả thực tế:

Không sao chép kết quả audit từ dự án tham khảo.
Không đánh dấu PASS khi chưa có bằng chứng kiểm thử.
