package com.example.videostreaming.service;

import com.example.videostreaming.dto.request.VideoCreateRequest;
import com.example.videostreaming.dto.response.UploadUrlResponse;
import com.example.videostreaming.dto.response.VideoResponse;
import com.example.videostreaming.entity.UploadStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service contract defining operations for video management.
 */
public interface VideoService {

    /**
     * Initiates the upload flow by creating a database stub record and
     * generating a pre-signed storage upload URL.
     *
     * @param contentType the mime-type of the video (e.g. video/mp4)
     * @return the upload target details DTO
     */
    UploadUrlResponse requestUploadUrl(String contentType);

    /**
     * Finalizes the video upload by verifying the physical file exists in storage
     * and saving metadata details.
     *
     * @param request the metadata payload containing title, description, and key
     * @return the created video metadata representation
     */
    VideoResponse createVideo(VideoCreateRequest request);

    /**
     * Retrieves a paginated list of videos, optionally filtered by upload status.
     *
     * @param status optional upload status filter
     * @param pageable pagination details
     * @return a page of VideoResponse records
     */
    Page<VideoResponse> getAllVideos(UploadStatus status, Pageable pageable);

    /**
     * Retrieves video metadata details by ID.
     *
     * @param id database ID of the video record
     * @return the VideoResponse payload
     */
    VideoResponse getVideoById(Long id);
}
