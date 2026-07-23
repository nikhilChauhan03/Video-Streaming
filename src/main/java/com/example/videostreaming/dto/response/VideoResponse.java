package com.example.videostreaming.dto.response;

import com.example.videostreaming.entity.UploadStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Payload returned to the client representing the saved Video metadata.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoResponse {

    private Long id;
    private String title;
    private String description;
    private String objectKey;
    private String bucket;
    private String contentType;
    private Long fileSize;
    private UploadStatus uploadStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
