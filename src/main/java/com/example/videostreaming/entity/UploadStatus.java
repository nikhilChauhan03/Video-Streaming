package com.example.videostreaming.entity;

/**
 * Represents the lifecycle status of a video in the platform.
 */
public enum UploadStatus {
    /**
     * Pre-signed upload URL has been generated for the client, but the file
     * has not yet been uploaded or metadata has not been confirmed.
     */
    PENDING_UPLOAD,

    /**
     * File has been successfully uploaded to MinIO/S3 and its existence is verified.
     */
    UPLOADED,

    /**
     * Video is currently undergoing server-side processing (e.g., transcoding, quality variations).
     */
    PROCESSING,

    /**
     * Video has been successfully processed, validated, and is ready for public consumption/streaming.
     */
    READY,

    /**
     * Upload verification or post-processing failed.
     */
    FAILED
}
