# Kết quả kiểm chứng Bước 5

**Học viên:** Nguyễn Vương Quốc Nam  
**Ngày kiểm chứng:** 29/10/2026, múi giờ Asia/Saigon

## Kết quả tổng thể tại local

Lệnh `mvnw.cmd -o -B -ntp clean verify` chạy toàn bộ suite và đóng gói thành
công. Chạy file JAR và script cURL xác nhận thêm các phản hồi HTTP thực tế.

| Nhóm | Tests/checks | Failure | Error | Skipped |
| --- | --- | --- | --- | --- |
| StudentRequestDTOTest | 40 | 0 | 0 | 0 |
| StudentResponseDTOTest | 7 | 0 | 0 | 0 |
| StudentApiIntegrationTest | 19 | 0 | 0 | 0 |
| StudentServiceImplTest | 12 | 0 | 0 | 0 |
| StudentRegistrationConcurrencyTest | 2 | 0 | 0 | 0 |
| Tổng JUnit | **80** | **0** | **0** | **0** |
| Script cURL trên JAR | **10** | **0** | Không áp dụng | **0** |

Số liệu JUnit được đọc từ các XML Surefire của lần chạy toàn bộ suite;
bản tổng hợp nằm trong `test-results.json`. Không cộng các lần chạy lẻ
vào tổng 80 và không quy đổi số lượt test thành phần trăm code coverage.

## Nội dung đã kiểm tra

### Dữ liệu đầu vào và masking

- Tất cả đầu số 03/05/07/08/09 hợp lệ theo regex đã chốt.
- Thiếu/null/trống, SĐT 9/11/12 số, ký tự `|`, ký tự chữ, mã quốc gia,
  khoảng trắng, xuống dòng và chữ số ngoài ASCII.
- CCCD đủ 12 số và giữ số 0; lỗi độ dài, ký tự hoặc chữ số ngoài ASCII.
- Email sai định dạng; họ tên thiếu hoặc trống.
- University/major có thể thiếu mà không tự thêm quy tắc ngoài đề.
- Constructor response tự masking, xử lý null/chuỗi ngắn và dữ liệu đã
  được masking; toString không chứa SĐT/CCCD nguyên bản.

### API và lưu trữ

- Đăng ký 201, UUID tự sinh, đủ tám trường và trạng thái PENDING_APPROVAL.
- Tra cứu 200 trả cùng cấu trúc và dữ liệu đã masking.
- Database lưu dữ liệu phục vụ nghiệp vụ; response chỉ hiển thị maskedPhone
  và maskedNationalId.
- Client gửi id/status cũng không chọn được UUID hoặc trạng thái duyệt.
- Trùng Email riêng hoặc CCCD riêng trả 409 và không tạo hồ sơ thứ hai.
- Validation 400 có errors object và đúng thông báo; dữ liệu lỗi không được lưu.
- UUID hợp lệ không tồn tại trả 404; UUID sai và JSON sai trả 400.
- HTTP method/content type không hỗ trợ giữ 405/415, không bị biến thành 500.

### Đồng thời và phân loại lỗi DB

Mỗi test đồng thời dùng hai transaction và H2 thật. Repository wrapper chỉ
đặt barrier trước saveAndFlush; toàn bộ exists/save vẫn chuyển tiếp vào
Repository thật. Cả hai request phải hoàn tất precheck trước khi ghi.

Cả hai trường hợp trùng Email và trùng CCCD đều xác nhận:

1. Một transaction đăng ký thành công.
2. Một transaction nhận StudentAlreadyExistsException đúng message.
3. Đếm được đúng một DataIntegrityViolationException từ H2 thật.
4. Database chỉ còn một hồ sơ đã commit và tra cứu được UUID của hồ sơ đó.

Test dùng CyclicBarrier và Future timeout, không dựa vào sleep hoặc giả lập
exists luôn false. Mockito chỉ phục vụ điểm đồng bộ và các nhánh lỗi unit;
integration test API dùng các bean ứng dụng và H2 thật.

Các unit test phân loại DB kiểm tra cả tên constraint, schema/hậu tố index,
SQLState khác unique, primary key, metadata thiếu và trường hợp dữ liệu mô
tả chứa tên constraint. Lỗi không liên quan được giữ nguyên, không gán 409.

## Nghiệm thu cURL trên file JAR

Đã chạy `scripts/Test-Api.ps1` với địa chỉ `http://127.0.0.1:8080`:

```text
PASS 01 POST 201, UUID, eight fields and masking
PASS 02 GET 200 matches registration response
PASS 03 validation 400 with field errors object
PASS 04 duplicate email 409
PASS 05 duplicate national ID 409
PASS 06 missing UUID 404
PASS 07 malformed UUID 400
PASS 08 malformed JSON 400
PASS 09 unsupported method remains 405
PASS 10 technical DB error stays 500 without SQL/trace/PII
RESULT 10/10 cURL checks passed
```

Check 10 cố ý gửi fullName dài hơn cột DB để kích hoạt lỗi lưu trữ. Đây là
kiểm tra phản hồi lỗi kỹ thuật, không phải quy tắc validation mới trong đề.
Lỗi giữ 500, không bị gán duplicate và không trả SQL, stacktrace, exception
hoặc CCCD, kể cả với `?trace=true&message=true`.

Script dùng Email/CCCD giả lập thay đổi theo lần chạy, file JSON UTF-8 tạm
và curl.exe thực tế; dọn file tạm khi kết thúc. Server kiểm chứng đã dừng.

## Điều chỉnh trong quá trình kiểm chứng

Lần chạy test tích hợp đầu dừng lâu sau khi Mockito tự attach agent trên
Java 21; lần chạy đó bị dừng và không được tính là PASS. Đã cấu hình
Mockito agent trong Surefire theo tài liệu chính thức, chỉ định thư mục
socket test vào target và đặt timeout cho tiến trình test. Chạy lại đạt
19 test tích hợp, sau đó toàn bộ 80 test đạt trong lần clean verify.

Không phát hiện lỗi nghiệp vụ cần thay đổi code main trong các kịch bản
đã chạy. Thay đổi của Bước 5 tập trung ở test, cấu hình chạy test, script
nghiệm thu và tài liệu. Không thay đổi sáu quyết định đã duyệt.

## Giới hạn và bằng chứng còn lại

- Chưa đo code coverage; không tuyên bố 100% coverage hoặc không còn mọi lỗi.
- Đã kiểm tra nội dung file ignore và phạm vi file local; chưa kiểm chứng
  hiệu lực chặn đọc của một công cụ AI cụ thể.
- Chưa tạo repository, commit hoặc PR GitHub cho bài này; không đánh dấu
  GitHub Evidence là hoàn thành.
- Xem `../AUDIT_CHECKLIST.md` để đối chiếu từng Quality Gate trong đề.

## Nguồn kỹ thuật

- [Spring Boot 3.5: Testing](https://docs.spring.io/spring-boot/3.5/reference/testing/spring-boot-applications.html).
- [JUnit 5.12.2: Parameterized Tests](https://docs.junit.org/5.12.2/user-guide/#writing-tests-parameterized-tests).
- [Mockito 5.17: thiết lập agent trên Java 21](https://javadoc.io/static/org.mockito/mockito-core/5.17.0/org.mockito/org/mockito/Mockito.html#0.3).
