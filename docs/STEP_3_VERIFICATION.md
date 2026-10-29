# Kết quả kiểm chứng Bước 3

**Học viên:** Nguyễn Vương Quốc Nam  
**Ngày kiểm chứng:** 29/10/2026, múi giờ Asia/Saigon  
**Phạm vi:** Khung package và tài liệu kiến trúc.

## Source và tài liệu đã tạo

- Bảy file `package-info.java` mô tả controller, dto, service,
  service.impl, entity, repository và shared.exception.
- `docs/ARCHITECTURE.md`: cây thư mục, danh mục lớp cho Bước 4,
  trách nhiệm, hợp đồng dữ liệu và hai luồng nghiệp vụ.
- `docs/decisions/001-student-module-layout.md`: lý do chọn cấu trúc.
- README và Dev Prompt Library được cập nhật theo Bước 3.

## Kết quả kiểm chứng

| Kiểm tra | Kết quả thực tế |
| --- | --- |
| Bảy package có tên khớp đường dẫn | Đạt |
| Package có mô tả trách nhiệm | Đạt |
| Lớp khởi động nằm ở package cha | Đạt, `com.r2s.masterclass` |
| Giới hạn thay đổi Java | Một lớp khởi động và bảy package-info; không có endpoint hoặc Entity |
| Tài liệu | UTF-8 hợp lệ, cặp code fence đầy đủ, liên kết file tương đối tồn tại |
| Maven verify offline, JDK 21 | BUILD SUCCESS |
| Bộ test nghiệp vụ | Chưa có; Maven báo No tests to run |

Lệnh build đã chạy:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21.0.12'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\mvnw.cmd -o -B -ntp verify
```

Log chính:

```text
Compiling 8 source files with javac [debug parameters release 21]
No tests to run.
BUILD SUCCESS
```

Không cần chạy lại server chỉ vì thêm mô tả package. Kết quả chạy server
và truy vấn H2 ở Bước 2 được giữ trong `STEP_2_VERIFICATION.md`, không ghi
nhận thành một lần kiểm chứng runtime mới của Bước 3.

## Đối chiếu yêu cầu đã chốt

- Request và response là Java Record trong thiết kế.
- Response POST/GET có đủ major và status, cùng tám trường.
- Lỗi validation dùng errors object.
- Regex SĐT giữ `^0[35789][0-9]{8}$`.
- Giữ Java 21, Spring Boot 3.x, JPA, Validation và H2; không thêm dependency.
- Các quy tắc ignore và cấu hình local giữ nguyên từ những bước trước.

Các mục trên đã được đối chiếu ở mức thiết kế. Chưa có bằng chứng runtime
cho validation, masking, chống trùng hoặc các HTTP status nghiệp vụ.

## Điểm dừng

Bước 3 hoàn thành khung kiến trúc. Bước 4 chờ người dùng duyệt trước khi
tạo các lớp triển khai nghiệp vụ; Bước 5 kiểm thử và audit tổng thể.
