package com.borrowapp.upload.service;

import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
    UploadResponse upload(MultipartFile file);
}