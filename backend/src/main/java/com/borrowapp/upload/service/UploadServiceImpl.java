package com.borrowapp.upload.service;

import com.borrowapp.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {

    private static final long MAX_SIZE = 5L * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    @Value("${upload.dir}")
    private String uploadDir;

    @Override
    public UploadResponse upload(MultipartFile file) {
        validateFile(file);

        String newFilename = generateFilename(file.getOriginalFilename());
        saveFile(file, newFilename);

        return new UploadResponse("/uploads/" + newFilename);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File không được để trống");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new BadRequestException(
                    "Chỉ chấp nhận file ảnh định dạng JPEG, PNG hoặc WEBP"
            );
        }

        if (file.getSize() > MAX_SIZE) {
            throw new BadRequestException("File không được vượt quá 5MB");
        }
    }

    /**
     * Tạo filename mới từ UUID + giữ nguyên extension gốc (lowercase).
     * Nếu file không có extension thì trả về UUID thuần.
     */
    private String generateFilename(String originalFilename) {
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename
                    .substring(originalFilename.lastIndexOf("."))
                    .toLowerCase();
        }
        return UUID.randomUUID().toString() + ext;
    }

    /**
     * Lưu file vào thư mục uploads/.
     * Tự tạo thư mục nếu chưa tồn tại.
     */
    private void saveFile(MultipartFile file, String filename) {
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path destination = uploadPath.resolve(filename);
            file.transferTo(destination);

        } catch (IOException e) {
            throw new RuntimeException("Lưu file thất bại: " + e.getMessage(), e);
        }
    }
}