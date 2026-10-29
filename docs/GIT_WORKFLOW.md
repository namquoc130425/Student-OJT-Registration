# Repository và quy trình Git

**Học viên:** Nguyễn Vương Quốc Nam  
**Repository:** [namquoc130425/Student-OJT-Registration](https://github.com/namquoc130425/Student-OJT-Registration)

Người dùng yêu cầu xuất bản dự án lên repository này, giữ ba nhánh và
đặt main làm nhánh chính/mặc định.

## Vai trò các nhánh

| Nhánh | Vai trò | Liên kết |
| --- | --- | --- |
| main | Nhánh chính, nhận bản tích hợp từ developer | [Mã nguồn main](https://github.com/namquoc130425/Student-OJT-Registration/tree/main) |
| developer | Nhánh tích hợp, nhận feature trước khi vào main | [Mã nguồn developer](https://github.com/namquoc130425/Student-OJT-Registration/tree/developer) |
| feature/student-ojt-registration | Phát triển tính năng và bộ test OJT | [Mã nguồn feature](https://github.com/namquoc130425/Student-OJT-Registration/tree/feature/student-ojt-registration) |

```text
feature/student-ojt-registration → developer → main
```

Tích hợp bằng `git merge --no-ff` để mỗi chặng có merge commit rõ ràng.
Giữ nguyên cả ba nhánh theo yêu cầu, không force-push hoặc xóa lịch sử.

## Các phần được tách commit

- Khởi tạo repository và file ignore.
- Cấu hình Java 21, Spring Boot, Maven Wrapper và H2.
- Hai API, DTO Record, masking, validation và xử lý lỗi.
- Bộ test và script cURL.
- Prompt, kiến trúc, audit và kết quả kiểm chứng.
- Tài liệu xuất bản và quy trình Git.

Mã nghiệp vụ đã được kiểm chứng trước khi tạo các commit: 80 test tự động
và 10 kịch bản cURL đạt. Các thay đổi xuất bản chỉ liên quan đến Git,
quy tắc xuống dòng và tài liệu; không thay đổi nghiệp vụ đã nghiệm thu.

## Bằng chứng và phạm vi công bố

- [Lịch sử merge trên main](https://github.com/namquoc130425/Student-OJT-Registration/commits/main/).
- [Dev Prompt Library](../Dev_Prompt_Library.md).
- [Audit checklist](../AUDIT_CHECKLIST.md).
- Cấu hình local `src/main/resources/application.yml`, `target/` và
  `output/` được Git ignore; chỉ chia sẻ cấu hình mẫu H2 công khai.
- GitHub connector báo 403 khi tạo nhánh/PR. Push và tích hợp dùng Git
  trực tiếp theo quyền người dùng đã cấp; không có PR được tạo.

Các báo cáo Bước 2–5 mô tả trạng thái tại thời điểm kiểm chứng trước khi
xuất bản. Tài liệu này cập nhật phần GitHub sau yêu cầu xuất bản của người dùng.
