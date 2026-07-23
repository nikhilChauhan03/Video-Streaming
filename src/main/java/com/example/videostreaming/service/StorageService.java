package com.example.videostreaming.service;

/**
 * Interface representing the storage service abstraction.
 * Business logic services interact only with this interface, ensuring the backend is decoupled
 * from concrete providers (MinIO, AWS S3, etc.).
 */
public interface StorageService {

    /**
     * Generates a pre-signed URL for uploading a file directly to the object store.
     *
     * @param objectKey          the destination key/path of the object
     * @param contentType        the HTTP content-type of the file (e.g. video/mp4)
     * @param expirationMinutes the duration in minutes until the link expires
     * @return the pre-signed upload URL
     */
    String generatePresignedUploadUrl(String objectKey, String contentType, int expirationMinutes);

    /**
     * Verifies whether an object exists in the storage bucket.
     *
     * @param objectKey the key/path of the object
     * @return true if the object exists, false otherwise
     */
    boolean doesObjectExist(String objectKey);

    /**
     * Retrieves the size of the stored object in bytes.
     *
     * @param objectKey the key/path of the object
     * @return object size in bytes
     */
    long getObjectSize(String objectKey);

    /**
     * Retrieves the content type (mime type) of the stored object.
     *
     * @param objectKey the key/path of the object
     * @return object content type
     */
    String getObjectContentType(String objectKey);
}
