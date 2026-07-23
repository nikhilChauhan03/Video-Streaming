package com.example.videostreaming.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Payload returned to the client containing details for uploading
 * a file directly to the object store.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadUrlResponse {

    private String uploadUrl;
    private String objectKey;
    private String bucket;
    private LocalDateTime expiresAt;
}
