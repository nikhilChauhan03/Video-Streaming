package com.example.videostreaming.service.impl;

import com.example.videostreaming.exception.StorageException;
import com.example.videostreaming.service.StorageService;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Concrete implementation of StorageService using the MinIO Java SDK.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MinioStorageService implements StorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    /**
     * Initializes the configured storage bucket.
     * Checks if the bucket exists at startup and automatically creates it if it does not.
     */
    @PostConstruct
    public void initializeBucket() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                log.info("MinIO bucket '{}' not found. Creating it...", bucketName);
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Successfully created MinIO bucket: {}", bucketName);
            } else {
                log.info("MinIO bucket '{}' already exists and is ready.", bucketName);
            }
        } catch (Exception e) {
            log.error("Failed to initialize MinIO bucket: {}", bucketName, e);
            throw new StorageException("Failed to initialize storage bucket: " + bucketName, e);
        }
    }

    @Override
    public String generatePresignedUploadUrl(String objectKey, String contentType, int expirationMinutes) {
        log.info("Generating pre-signed upload URL for objectKey: '{}', contentType: '{}'", objectKey, contentType);
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucketName)
                            .object(objectKey)
                            .expiry(expirationMinutes, TimeUnit.MINUTES)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to generate pre-signed upload URL for objectKey: {}", objectKey, e);
            throw new StorageException("Error generating pre-signed upload URL", e);
        }
    }

    @Override
    public boolean doesObjectExist(String objectKey) {
        log.debug("Checking if object exists in bucket '{}': '{}'", bucketName, objectKey);
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build()
            );
            return true;
        } catch (io.minio.errors.ErrorResponseException e) {
            // NoSuchKey is the standard S3 error code returned when the object does not exist.
            if ("NoSuchKey".equals(e.errorResponse().code()) || "NoSuchBucket".equals(e.errorResponse().code())) {
                return false;
            }
            log.error("MinIO error response checking existence of '{}'", objectKey, e);
            throw new StorageException("Failed to check object existence in storage", e);
        } catch (Exception e) {
            log.error("Failed checking existence of objectKey: {}", objectKey, e);
            throw new StorageException("Error checking object existence in storage", e);
        }
    }

    @Override
    public long getObjectSize(String objectKey) {
        log.debug("Fetching size for objectKey '{}' in bucket '{}'", objectKey, bucketName);
        try {
            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build()
            ).size();
        } catch (Exception e) {
            log.error("Failed to retrieve size for objectKey: {}", objectKey, e);
            throw new StorageException("Failed to retrieve object size", e);
        }
    }

    @Override
    public String getObjectContentType(String objectKey) {
        log.debug("Fetching content type for objectKey '{}' in bucket '{}'", objectKey, bucketName);
        try {
            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build()
            ).contentType();
        } catch (Exception e) {
            log.error("Failed to retrieve content type for objectKey: {}", objectKey, e);
            throw new StorageException("Failed to retrieve object content type", e);
        }
    }
}
