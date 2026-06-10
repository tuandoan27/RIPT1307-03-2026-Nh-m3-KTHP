Frontend (Netlify): https://borrowapp-fe.netlify.app/

Hướng dẫn cài đặt và chạy dự án

Yêu cầu
Java 17+
PostgreSQL 16+
Node.js 18+

1. Database
Mở pgAdmin, tạo database tên borrowapp
Chuột phải vào borrowapp → Restore → chọn file database/dump.sql → Restore

2. Backend
Mở file backend/src/main/resources/application.properties, sửa password PostgreSQL:

spring.datasource.password=your_password
Chạy:

cd backend
.\mvnw.cmd spring-boot:run -DskipTests
Backend chạy tại: http://localhost:8080

Tài khoản demo

Admin	admin@gmail.com	123456
Sinh viên	đăng ký tài khoản
