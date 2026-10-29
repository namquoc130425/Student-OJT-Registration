# Kết quả kiểm chứng Bước 2

**Học viên:** Nguyễn Vương Quốc Nam  
**Ngày kiểm chứng:** 29/10/2026, múi giờ Asia/Saigon  
**Phạm vi:** Khởi tạo source, cấu hình Maven và kết nối H2.

## Môi trường và phiên bản

| Thành phần | Kết quả thực tế |
| --- | --- |
| JDK | Oracle JDK 21.0.12 |
| Spring Boot | 3.5.16 |
| Maven qua Wrapper | 3.9.15 |
| Maven Wrapper | 3.3.4, only-script |
| H2 | 2.3.232 |
| Địa chỉ ứng dụng | `http://127.0.0.1:8080` |
| Database | `jdbc:h2:mem:ojtdb` |

JDK 21 có tại `C:\Program Files\Java\jdk-21.0.12`.
`JAVA_HOME` ban đầu của máy trỏ tới JDK 24; các lệnh kiểm chứng đặt
`JAVA_HOME` về JDK 21 trong terminal hiện tại.

## Kết quả

| Kiểm tra | Kết quả | Bằng chứng |
| --- | --- | --- |
| Maven Wrapper | Đạt | `mvnw.cmd -version` báo Maven 3.9.15, Java 21.0.12 |
| Biên dịch và đóng gói | Đạt | `mvnw.cmd -B -ntp clean verify` trả `BUILD SUCCESS` |
| Java target | Đạt | Maven compiler báo `release 21` |
| Chạy file JAR | Đạt với tham số socket bên dưới | Log `Started StudentOjtApplication`, Tomcat port 8080 |
| Chạy bằng Maven | Đạt với tham số socket bên dưới | `spring-boot:run` khởi động ứng dụng và kết nối H2 |
| Hikari/JPA | Đạt | Pool thêm kết nối tới `jdbc:h2:mem:ojtdb`, JPA EntityManagerFactory khởi tạo |
| H2 Console | Đạt | HTTP 200 tại `/h2-console/` |
| Truy vấn database đang chạy | Đạt | `SELECT 1` trả 1; catalog `OJTDB`, H2 `2.3.232` |
| Test nghiệp vụ | Chưa thực hiện | Chưa có API nghiệp vụ; Maven báo `No tests to run` |

`BUILD SUCCESS` ở bước này là kết quả build và đóng gói, không phải bằng
chứng test nghiệp vụ đã đạt.

## Các lệnh kiểm chứng

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21.0.12'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\mvnw.cmd -version
.\mvnw.cmd -B -ntp clean verify
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.jvmArguments=-Djdk.net.unixdomain.tmpdir=target'
```

Đường chạy JAR cũng đã được kiểm tra:

```powershell
java '-Djdk.net.unixdomain.tmpdir=target' -jar 'target/student-ojt-registration-0.0.1-SNAPSHOT.jar'
```

Kết quả chính của build:

```text
Compiling 1 source file with javac [debug parameters release 21]
No tests to run.
BUILD SUCCESS
```

## Kiểm chứng H2

Đã mở H2 Console bằng HTTP, đăng nhập vào URL
`jdbc:h2:mem:ojtdb;IFEXISTS=TRUE` với tài khoản mẫu `sa`, mật khẩu trống.
`IFEXISTS=TRUE` bảo đảm lần kiểm tra sử dụng database đã được ứng dụng
khởi tạo, không tự tạo một database khác để làm sai lệch kết quả.

Đã thực hiện:

```sql
SELECT 1 AS CONNECTION_CHECK,
       H2VERSION() AS H2_VERSION,
       CURRENT_CATALOG AS DATABASE_NAME;
```

| CONNECTION_CHECK | H2_VERSION | DATABASE_NAME |
| --- | --- | --- |
| 1 | 2.3.232 | OJTDB |

Đã đăng xuất phiên H2 Console sau kiểm tra.

## Vấn đề môi trường đã xử lý

Lần chạy đầu khởi tạo H2 thành công nhưng HTTP server không khởi động
được. Trong sandbox, lỗi gốc là `Permission denied: connect`; khi cho phép
network, Java NIO báo `Unable to establish loopback connection`, kèm
`Invalid argument: connect` trong `UnixDomainSockets.connect`.

Đã thu hẹp bằng chương trình Java tối thiểu gọi `Selector.open()`.
Khi đặt `-Djdk.net.unixdomain.tmpdir` vào thư mục `target` của dự án,
Selector mở thành công. Chạy lại cùng file JAR với tham số này khởi động
được Tomcat và truy vấn H2 thành công. Đã kiểm tra thêm đường chạy qua
Maven với `spring-boot.run.jvmArguments` để README có lệnh tái hiện.

Điều chỉnh chỉ áp dụng cho tiến trình kiểm chứng. Không sửa JDK, registry,
firewall hay biến môi trường toàn máy.

## Phạm vi bàn giao

- `pom.xml`: Java 21, Spring Boot parent và các dependency đã duyệt.
- `mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties`: Maven Wrapper.
- `StudentOjtApplication.java`: lớp khởi động duy nhất.
- `config/application.example.yml`: cấu hình H2 mẫu có thể chia sẻ.
- `src/main/resources/application.yml`: bản local tạo từ mẫu, nằm trong ignore.
- `README.md`: hướng dẫn cấu hình, build và chạy trên Windows.
- `Dev_Prompt_Library.md`: prompt và nhật ký audit Bước 2.

Kiến trúc các lớp nghiệp vụ chờ duyệt Bước 3. Hai API và bộ test nghiệp vụ
chưa nằm trong phạm vi Bước 2.

## Tài liệu đối chiếu

- [Spring Boot 3.5: System Requirements](https://docs.spring.io/spring-boot/3.5/system-requirements.html)
- [Spring Boot 3.5: SQL Databases](https://docs.spring.io/spring-boot/3.5/reference/data/sql.html)
- [Spring Boot: Running with Maven](https://docs.spring.io/spring-boot/3.5/maven-plugin/run.html)
- [Apache Maven Wrapper](https://maven.apache.org/tools/wrapper/)
- [Java 21: Networking Properties](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/net/doc-files/net-properties.html)
