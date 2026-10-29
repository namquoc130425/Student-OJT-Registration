# Student OJT Registration Service



## Công nghệ đã chọn

| Thành phần | Phiên bản hoặc cấu hình |
| --- | --- |
| Java | 21 |
| Spring Boot | 3.5.16 |
| Maven Wrapper | Maven 3.9.15 |
| Web | Spring Web, embedded Tomcat |
| Database | H2 in-memory, Spring Data JPA |
| Validation | Jakarta Validation |
| Test | Spring Boot Starter Test |

`spring-boot-starter-parent` quản lý phiên bản dependency đồng bộ với BOM
Spring Boot. Không khai báo riêng phiên bản Hibernate, H2 hoặc Jakarta
Validation và không thêm Lombok.

## Chuẩn bị trên Windows PowerShell

Mở terminal tại thư mục gốc dự án. Cần JDK 21; Maven Wrapper sẽ cung cấp
Maven đúng phiên bản, không bắt buộc cài Maven riêng.

Trên máy hiện tại có thể chọn JDK 21 cho terminal đang dùng:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21.0.12'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
.\mvnw.cmd -version
```

Trên máy khác, thay đường dẫn `JAVA_HOME` bằng thư mục cài JDK 21.
Các lệnh trên chỉ thay đổi môi trường của terminal hiện tại.
Lần chạy Maven Wrapper đầu tiên cần mạng để tải Maven và dependency.

## Tạo cấu hình local

`config/application.example.yml` là cấu hình mẫu công khai, chỉ có thông tin
database thực hành. Tạo cấu hình local nếu file chưa tồn tại:

```powershell
New-Item -ItemType Directory -Path 'src/main/resources' -Force | Out-Null
if (-not (Test-Path -LiteralPath 'src/main/resources/application.yml')) {
    Copy-Item -LiteralPath 'config/application.example.yml' -Destination 'src/main/resources/application.yml'
}
```

File local nằm trong `.gitignore` và `.copilotignore`. `.gitignore` chỉ kiểm
soát việc theo dõi file bằng Git; hiệu lực loại trừ của công cụ AI cần được
kiểm tra riêng với công cụ sử dụng. Không đưa thông tin kết nối thật vào
file cấu hình mẫu.

## Build và chạy

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
```

Ứng dụng chạy tại `http://127.0.0.1:8080`. Dùng `Ctrl+C` để dừng.
Nếu cổng 8080 đang được dùng, có thể chọn cổng khác cho lần chạy:

```powershell
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.arguments=--server.port=8081'
```


## Kiểm tra kết nối H2

Mở [H2 Console](http://127.0.0.1:8080/h2-console/) khi ứng dụng đang chạy:

| Trường | Giá trị thực hành |
| --- | --- |
| Driver Class | `org.h2.Driver` |
| JDBC URL | `jdbc:h2:mem:ojtdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE` |
| User Name | `sa` |
| Password | Để trống |

Chạy `SELECT 1 AS connection_check;` để xác nhận truy vấn database thành công.
Database nằm trong bộ nhớ và mất dữ liệu khi tắt tiến trình ứng dụng.
Bảng hồ sơ là `student_registrations`. Database có unique constraint riêng
cho Email và CCCD.

## Sử dụng API

| API | Kết quả thành công |
| --- | --- |
| `POST /api/v1/students/register` | HTTP 201, hồ sơ đủ 8 trường và đã masking |
| `GET /api/v1/students/{id}` | HTTP 200, cùng cấu trúc response đăng ký |

Dữ liệu mẫu giả lập nằm trong `examples/register-student.json`.
Sau khi khởi động ứng dụng, gửi request bằng PowerShell:

```powershell
curl.exe -i -X POST 'http://127.0.0.1:8080/api/v1/students/register' -H 'Content-Type: application/json' --data-binary '@examples/register-student.json'
```

Response chứa UUID tự sinh, major, trạng thái `PENDING_APPROVAL`,
`maskedPhone` là `090*****67` và `maskedNationalId` là `079******789`.
Dùng UUID nhận được thay cho `<id>` để tra cứu:

```powershell
curl.exe -i 'http://127.0.0.1:8080/api/v1/students/<id>'
```

Gửi lại dữ liệu có Email hoặc CCCD đã đăng ký sẽ trả 409. Validation thất
bại trả 400 với `errors` là object theo trường. UUID hợp lệ nhưng không có
hồ sơ trả 404. Không có API danh sách, sửa, xóa hoặc duyệt hồ sơ trong đề.

## Kiểm thử và audit

Chạy toàn bộ test và đóng gói bằng JDK 21:

```powershell
.\mvnw.cmd clean verify
```

Test dùng H2 riêng `ojt_test`, tự dọn dữ liệu giữa các test. Mockito agent
được Surefire nạp khi JVM test khởi động để chạy ổn định trên Java 21.
Báo cáo chi tiết sinh tại `target/surefire-reports/`.

Để nghiệm thu HTTP bằng cURL, khởi động ứng dụng theo hướng dẫn bên trên,
mở terminal PowerShell 7 khác tại thư mục dự án rồi chạy:

```powershell
.\scripts\Test-Api.ps1
```

Script thực hiện 10 check, dùng dữ liệu giả lập và gửi cả request cố ý gây
lỗi DB để kiểm tra 500 không lộ thông tin. Script tạo một hồ sơ thực hành
trong H2; dừng và chạy lại ứng dụng để xóa dữ liệu in-memory khi cần.

- [Audit checklist theo đề](AUDIT_CHECKLIST.md).
- [Báo cáo và phạm vi kiểm chứng](docs/STEP_5_VERIFICATION.md).
- [Số liệu test đã chạy](docs/test-results.json).

Source và bốn sản phẩm bài nộp đã có tại local. Bằng chứng GitHub và hiệu
lực chặn đọc thực tế của công cụ AI được ghi riêng là chưa xác minh.

## Kiến trúc source

```text
com.r2s.masterclass
├── StudentOjtApplication.java
├── student
│   ├── controller
│   ├── dto
│   ├── service
│   │   └── impl
│   ├── entity
│   └── repository
└── shared
    └── exception
```

Các package có `package-info.java` và các lớp đã triển khai. Controller gọi interface service,
Service xử lý nghiệp vụ và Repository truy cập H2. Response đi qua DTO
Record có masking, không trả Entity ra HTTP.

- [Danh mục lớp và hợp đồng dữ liệu](docs/ARCHITECTURE.md).
- [Quyết định tổ chức module](docs/decisions/001-student-module-layout.md).

## Các bước thực hiện

| Bước | Nội dung |
| --- | --- |
| 1 | Dev Prompt Library và file ignore |
| 2 | Khởi tạo ứng dụng, Maven và kết nối H2 |
| 3 | Cấu trúc các lớp và module nghiệp vụ |
| 4 | Hai API, validation, masking và xử lý lỗi |
| 5 | Kiểm thử nghiệp vụ, audit và hoàn thiện bài nộp |

Mỗi bước được trình người dùng duyệt trước khi thực hiện.
Kết quả kiểm chứng được ghi trong `docs/STEP_2_VERIFICATION.md`.
Khung kiến trúc và kết quả build Bước 3 được ghi tại
[báo cáo Bước 3](docs/STEP_3_VERIFICATION.md).
Kết quả build và chạy thử hai API xem tại
[báo cáo Bước 4](docs/STEP_4_VERIFICATION.md).
Kết quả nghiệm thu local xem tại [báo cáo Bước 5](docs/STEP_5_VERIFICATION.md).

## Tài liệu chính thức

- [Yêu cầu hệ thống Spring Boot 3.5](https://docs.spring.io/spring-boot/3.5/system-requirements.html)
- [Cấu hình SQL, JPA và H2](https://docs.spring.io/spring-boot/3.5/reference/data/sql.html)
- [Apache Maven Wrapper](https://maven.apache.org/tools/wrapper/)
- [Truyền JVM arguments khi chạy bằng Maven](https://docs.spring.io/spring-boot/3.5/maven-plugin/run.html)
- [Networking properties Java 21](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/net/doc-files/net-properties.html)
