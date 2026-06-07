# Hướng dẫn cài đặt và chạy dự án

## Yêu cầu
- Java 17+
- PostgreSQL 16+
- Node.js 18+

---

## 1. Database

1. Mở pgAdmin, tạo database tên `borrowapp`
2. Chuột phải vào `borrowapp` → **Restore** → chọn file `database/dump.sql` → **Restore**

---

## 2. Backend

Mở file `backend/src/main/resources/application.properties`, sửa password PostgreSQL:

```properties
spring.datasource.password=your_password
```

Chạy:

```bash
cd backend
.\mvnw.cmd spring-boot:run -DskipTests
```

Backend chạy tại: `http://localhost:8080`

---

## Tài khoản demo

| Vai trò | Email | Mật khẩu |
|---------|-------|----------|
| Admin | admin@borrowapp.com | 123456 |
| Sinh viên | an@gmail.com | 123456 |