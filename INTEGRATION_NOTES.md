# Integration Notes — Notification + ActivityLog vào project của Tuấn

## Tóm tắt

Đã merge module `notification` + `activity` (từ `backend__2_.zip`) vào project gốc
của Tuấn, đồng thời tích hợp gửi email và ghi activity log vào workflow
`BorrowRequestService` (approve / reject / return).

## Endpoint mới (Notification – user)

| Method | URL                              | Mô tả                                          |
|:------:|----------------------------------|-----------------------------------------------|
| GET    | `/api/notifications`             | Danh sách thông báo, phân trang (`page`, `pageSize`), trả thêm `unreadCount` |
| PUT    | `/api/notifications/{id}/read`   | Đánh dấu 1 thông báo là đã đọc (kiểm tra ownership) |
| PUT    | `/api/notifications/read-all`    | Đánh dấu tất cả thông báo của user hiện tại là đã đọc |

> `page` 1-based để đồng nhất với `BorrowRequestController`.

## Endpoint mới (Admin)

| Method | URL                                              | Mô tả                                |
|:------:|--------------------------------------------------|--------------------------------------|
| GET    | `/api/admin/notifications/failed-emails`         | Danh sách email gửi thất bại         |
| POST   | `/api/admin/notifications/retry-email/{id}`      | Retry 1 email theo `NotificationLog` ID |
| POST   | `/api/admin/notifications/retry-all-failed`      | Retry toàn bộ email FAILED (max 100/lần) |
| GET    | `/api/activity-logs`                             | Xem activity log có filter + phân trang |

## EmailService – async fire-and-forget

- `EmailServiceImpl.sendAsync(to, subject, htmlBody)` chạy trên thread pool
  riêng `emailTaskExecutor` (`AsyncConfig`).
- Trước khi gửi, tạo `NotificationLog` với `status=FAILED` để đảm bảo có
  record dù JVM crash.
- Gửi thành công → `markSuccess()`. Lỗi → `incrementRetry(errorMessage)`,
  **không re-throw** → không ảnh hưởng request chính.
- `EmailRetryScheduler` chạy mỗi 15 phút tự động retry các log
  `FAILED` có `retry_count < 5`.

## Tích hợp vào `BorrowRequestService`

- `approveRequest`
  - ✅ Activity log: `REQUEST_APPROVED` (actor = admin đang đăng nhập)
  - ✅ Email + in-app notification cho người mượn (`NotificationType.REQUEST_APPROVED`)
- `rejectRequest` (đã thêm `@Transactional`)
  - ✅ Activity log: `REQUEST_REJECTED` (detail có cả `reason`)
  - ✅ Email + in-app notification (`NotificationType.REQUEST_REJECTED`)
- `returnRequest`
  - ✅ Activity log: `REQUEST_RETURNED` (không gửi email theo yêu cầu)

Email HTML có template gọn (Vietnamese, có HTML-escaping).

## Các điều chỉnh đáng chú ý

1. **`ActivityLogAction`** (`common/constants`): thêm `RETRY_EMAIL` để
   `EmailServiceImpl.retryAsync` ghi log được. Giữ nguyên các giá trị
   `REQUEST_APPROVED / REQUEST_REJECTED / REQUEST_RETURNED` đã có.
2. **Exceptions trong notification module**:
   - `EntityNotFoundException` → `ResourceNotFoundException`
   - `new SecurityException(...)` → `new ForbiddenException(...)`
   - `new IllegalArgumentException(...)` (retry log missing) → `new ResourceNotFoundException(...)`
   → GlobalExceptionHandler của Tuấn sẽ map đúng 404 / 403.
3. **KHÔNG copy** `SecurityConfig` và `GlobalExceptionHandler` của module
   `backend__2_` (xung đột với cấu hình hiện tại của Tuấn).
4. **NotificationController** dùng `SecurityContextHolder` để lấy email rồi
   `UserRepository.findByEmail` → tương thích với `JwtAuthFilter` của Tuấn
   (principal name = email).
5. **Schema**: dựa vào `spring.jpa.hibernate.ddl-auto=update` để Hibernate tự
   tạo bảng `activity_logs`, `notifications`, `notification_logs`.
   Nếu muốn schema chuẩn (CHECK constraints, indexes như V4/V5.sql),
   cần thêm `flyway-core` vào `pom.xml` và copy hai file SQL vào
   `src/main/resources/db/migration/`.

## Việc người dùng cần làm sau khi nhận code

1. Cấu hình Gmail SMTP thật trong `application.properties`:
   ```
   spring.mail.username=<your_real_email>@gmail.com
   spring.mail.password=<gmail_app_password>
   ```
   (App password lấy từ Google Account → Security → App passwords)
2. Khởi chạy app → kiểm tra console log "Started BorrowappApplication".
3. Login với tài khoản ADMIN → gọi `PUT /api/requests/{id}/approve` →
   kiểm tra:
   - User nhận được email duyệt yêu cầu.
   - Bảng `notifications` có row mới (status `UNREAD`).
   - Bảng `activity_logs` có row `action=REQUEST_APPROVED`.
   - Nếu mail server lỗi: `notification_logs` có row `status=FAILED`,
     scheduler sẽ tự retry sau 15 phút.

## Test nhanh bằng curl

```bash
# 1. Login (lấy JWT)
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"student@example.com","password":"xxx"}' \
  | jq -r '.data.token')

# 2. Lấy danh sách notification
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/notifications?page=1&pageSize=10"

# 3. Mark 1 cái là đã đọc
curl -X PUT -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/notifications/1/read"

# 4. Mark tất cả đã đọc
curl -X PUT -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/notifications/read-all"
```
