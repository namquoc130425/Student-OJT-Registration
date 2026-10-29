# Audit checklist Student OJT Registration Service

**Học viên:** Nguyễn Vương Quốc Nam  
**Ngày kiểm chứng:** 29/10/2026, múi giờ Asia/Saigon

Code và API đã đạt bộ nghiệm thu local: **80 test tự động và 10 kịch bản
cURL**, không có test lỗi hoặc bị bỏ qua. Bảng này phân biệt bằng chứng
local với yêu cầu liên quan đến công cụ AI và GitHub chưa được xác minh.

## Đối chiếu Quality Gate trong đề

| STT | Hạng mục | Trạng thái | Bằng chứng và giới hạn |
| --- | --- | --- | --- |
| 1 | Red-Zone Security | Đạt cấu hình file; hiệu lực công cụ AI chưa xác minh | Có `.copilotignore` đủ quy tắc, `.gitignore` riêng; cấu hình local khớp mẫu H2 công khai, không có bí mật thật trong phạm vi đã kiểm tra. Chưa thử khả năng chặn đọc thực tế của một công cụ AI cụ thể. |
| 2 | Controller chỉ nhận/trả DTO Record | Đạt local | Controller dùng StudentRequestDTO/StudentResponseDTO; không import Entity/Repository; integration test kiểm tra JSON đúng tám trường. |
| 3 | Masking PII tự động | Đạt local | 7 test response DTO, test POST/GET và cURL xác nhận không có SĐT/CCCD gốc trong response. |
| 4 | Jakarta Validation | Đạt local | 40 test request DTO; test API kiểm tra errors object, thông báo tiếng Việt và dữ liệu thiếu/null/trống. |
| 5 | Duplicate Business Logic | Đạt local | Trùng riêng Email/CCCD trả 409; 2 test đồng thời buộc phát sinh lỗi unique thật trên H2, chỉ một hồ sơ được commit. |
| 6 | Exception Handling | Đạt local | 400/409/404 đúng contract; 405/415 giữ status; cURL kiểm tra lỗi DB kỹ thuật trả 500 và không lộ SQL/trace/PII dù client yêu cầu trace/message. |
| 7 | Prompt Evidence | Đạt file local; GitHub chưa thực hiện | Dev_Prompt_Library.md có R-C-T-C-O và nhật ký từng bước. Chưa có repository/PR GitHub của bài nộp này. |

Chưa tuyên bố **100% toàn bộ DoD**, vì hiệu lực bảo vệ của công cụ AI và
bằng chứng GitHub chưa được xác minh. Không tự push code vào các repository
tham khảo của người khác.

## Bốn sản phẩm bài nộp tại local

- [x] Source code Java Spring Boot và Maven Wrapper.
- [x] Audit checklist có trạng thái và bằng chứng thực tế.
- [x] [Dev Prompt Library](Dev_Prompt_Library.md).
- [x] File `.copilotignore` tại root; `.gitignore` phục vụ Git riêng biệt.

## Kiểm tra bổ sung

- [x] Request và response dùng Java Record, không dùng Lombok.
- [x] POST và GET đều có major và status.
- [x] Regex SĐT đúng `^0[35789][0-9]{8}$`, không chấp nhận ký tự `|`.
- [x] SĐT 10 số, CCCD 12 số, giữ được số 0 ở đầu.
- [x] ID và trạng thái do hệ thống sinh, client không tự chọn được.
- [x] Unique constraint hoạt động khi hai transaction đã vượt qua precheck.
- [x] Lỗi DB không liên quan không bị chuyển nhầm thành duplicate.
- [x] H2 test riêng, dọn dữ liệu giữa các test; concurrency có timeout.
- [x] JDK 21 chạy Maven; Mockito agent được cấu hình trước khi JVM test khởi động.
- [x] Không có quan hệ JPA hoặc truy vấn lặp gây N+1 trong phạm vi hai use case.
- [x] Tiến trình server kiểm chứng đã dừng sau nghiệm thu.

## Bằng chứng

- [Báo cáo Bước 5](docs/STEP_5_VERIFICATION.md).
- [Số liệu test](docs/test-results.json).
- [Script cURL nghiệm thu](scripts/Test-Api.ps1).
- Chi tiết JUnit chạy lại bằng `mvnw.cmd clean verify`, báo cáo sinh tại
  `target/surefire-reports/`.
