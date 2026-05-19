package com.couple.service.impl;

import com.couple.exception.BusinessException;
import com.couple.service.FileService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private static final Set<String> IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp");
    private static final Set<String> VIDEO_TYPES = Set.of(
            "video/mp4", "video/webm", "video/ogg");

    private final MinioClient minioClient;

    @Value("${app.minio.bucket}")
    private String bucket;

    @Value("${app.minio.endpoint}")
    private String endpoint;

    @Value("${app.file.image-max-size}")
    private long imageMaxSize;

    @Value("${app.file.video-max-size}")
    private long videoMaxSize;

    public FileServiceImpl(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public Map<String, String> upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("文件为空");
        }

        String contentType = file.getContentType();
        boolean isImage = IMAGE_TYPES.contains(contentType);
        boolean isVideo = VIDEO_TYPES.contains(contentType);

        if (!isImage && !isVideo) {
            throw new BusinessException("仅支持图片(jpg/png/gif/webp)和视频(mp4/webm/ogg)格式");
        }

        long maxSize = isImage ? imageMaxSize : videoMaxSize;
        if (file.getSize() > maxSize) {
            String limit = isImage ? "10MB" : "100MB";
            throw new BusinessException("文件大小超过限制(" + limit + ")");
        }

        // Build object path: yyyy/MM/dd/uuid.ext
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String objectName = dateDir + "/" + UUID.randomUUID().toString().replace("-", "")
                + getExtension(file.getOriginalFilename());

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }

        // Public URL (assumes bucket is public-read)
        String fileUrl = endpoint + "/" + bucket + "/" + objectName;

        Map<String, String> result = new HashMap<>();
        result.put("url", fileUrl);
        result.put("type", isImage ? "image" : "video");
        return result;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }
}
