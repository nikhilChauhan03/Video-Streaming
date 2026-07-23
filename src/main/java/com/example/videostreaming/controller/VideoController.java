package com.example.videostreaming.controller;

import com.example.videostreaming.dto.request.VideoCreateRequest;
import com.example.videostreaming.dto.response.ApiResponse;
import com.example.videostreaming.dto.response.UploadUrlResponse;
import com.example.videostreaming.dto.response.VideoResponse;
import com.example.videostreaming.entity.UploadStatus;
import com.example.videostreaming.service.VideoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller exposing endpoints for orchestrating video uploads.
 */
@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
@Validated
@Slf4j
public class VideoController {

    private final VideoService videoService;

    /**
     * Generates a pre-signed URL for direct upload to storage.
     * Exposes GET /api/videos/upload-url?contentType=video/mp4
     *
     * @param contentType the target file mime-type
     * @return the pre-signed URL payload
     */
    @GetMapping("/upload-url")
    public ApiResponse<UploadUrlResponse> getUploadUrl(
            @RequestParam @NotBlank(message = "contentType is required") String contentType) {

        if (!contentType.toLowerCase().startsWith("video/")) {
            throw new IllegalArgumentException("Invalid content type. Only video mime-types are allowed.");
        }

        log.info("Received request for upload URL with contentType: '{}'", contentType);
        UploadUrlResponse response = videoService.requestUploadUrl(contentType);
        return ApiResponse.success("Upload URL generated successfully", response);
    }

    /**
     * Finalizes and confirms the video upload, verifying file existence.
     * Exposes POST /api/videos
     *
     * @param request metadata details for the video record
     * @return saved video metadata
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<VideoResponse> createVideo(@Valid @RequestBody VideoCreateRequest request) {
        log.info("Received request to confirm video upload for objectKey: '{}'", request.getObjectKey());
        VideoResponse response = videoService.createVideo(request);
        return ApiResponse.success("Video metadata saved and upload verified successfully", response);
    }

    /**
     * Retrieves a paginated list of videos, optionally filtered by upload status.
     * Exposes GET /api/videos
     *
     * @param status optional status to filter by
     * @param page page index (0-based)
     * @param size page size
     * @param sortBy property to sort by
     * @param direction sort direction (ASC or DESC)
     * @return a paginated wrapper containing list of video metadata
     */
    @GetMapping
    public ApiResponse<Page<VideoResponse>> getAllVideos(
            @RequestParam(required = false) UploadStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        log.info("Received request to list videos. status: '{}', page: '{}', size: '{}', sortBy: '{}', direction: '{}'",
                status, page, size, sortBy, direction);

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<VideoResponse> response = videoService.getAllVideos(status, pageRequest);
        return ApiResponse.success("Videos retrieved successfully", response);
    }

    /**
     * Retrieves video metadata by its database ID.
     * Exposes GET /api/videos/{id}
     *
     * @param id database ID of the video
     * @return video metadata details
     */
    @GetMapping("/{id}")
    public ApiResponse<VideoResponse> getVideoById(@PathVariable Long id) {
        log.info("Received request to fetch video with ID: '{}'", id);
        VideoResponse response = videoService.getVideoById(id);
        return ApiResponse.success("Video details retrieved successfully", response);
    }
}
