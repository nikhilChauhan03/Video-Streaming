package com.example.videostreaming.service.impl;

import com.example.videostreaming.dto.request.VideoCreateRequest;
import com.example.videostreaming.dto.response.UploadUrlResponse;
import com.example.videostreaming.dto.response.VideoResponse;
import com.example.videostreaming.entity.UploadStatus;
import com.example.videostreaming.entity.User;
import com.example.videostreaming.entity.Video;
import com.example.videostreaming.exception.ResourceNotFoundException;
import com.example.videostreaming.repository.VideoRepository;
import com.example.videostreaming.security.SecurityContextService;
import com.example.videostreaming.service.StorageService;
import com.example.videostreaming.service.VideoService;
import com.example.videostreaming.service.ChannelService;
import com.example.videostreaming.entity.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Service implementation for managing the video upload lifecycle.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VideoServiceImpl implements VideoService {

    private final StorageService storageService;
    private final VideoRepository videoRepository;
    private final SecurityContextService securityContextService;
    private final ChannelService channelService;

    @Value("${minio.bucket-name}")
    private String bucketName;

    // Pre-signed URL expiration time (e.g. 15 minutes)
    private static final int EXPIRATION_MINUTES = 15;

    @Override
    @Transactional
    public UploadUrlResponse requestUploadUrl(String contentType, Long channelId) {
        log.info("Requesting upload URL for content-type: '{}' in channel: '{}'", contentType, channelId);

        User currentUser = securityContextService.getCurrentUser();
        Channel channel = channelService.getChannelEntityById(channelId);

        // Enforce channel ownership for uploading
        if (!channel.getOwner().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("You do not own this channel.");
        }

        String extension = getFileExtension(contentType);
        String objectKey = generateObjectKey(extension);

        // Generate the pre-signed URL pointing to MinIO
        String uploadUrl = storageService.generatePresignedUploadUrl(objectKey, contentType, EXPIRATION_MINUTES);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);

        // Pre-create the video stub record in the database with PENDING_UPLOAD status
        Video video = Video.builder()
                .title("Untitled Upload")
                .description("Upload pending client action")
                .objectKey(objectKey)
                .bucket(bucketName)
                .contentType(contentType)
                .uploadStatus(UploadStatus.PENDING_UPLOAD)
                .user(currentUser)
                .channel(channel)
                .build();

        videoRepository.save(video);
        log.info("Created pending video stub record for objectKey: '{}' in channel: '{}'", objectKey, channelId);

        return UploadUrlResponse.builder()
                .uploadUrl(uploadUrl)
                .objectKey(objectKey)
                .bucket(bucketName)
                .expiresAt(expiresAt)
                .build();
    }

    @Override
    @Transactional
    public VideoResponse createVideo(VideoCreateRequest request) {
        log.info("Confirming upload and creating video metadata for objectKey: '{}'", request.getObjectKey());

        // 1. Fetch the video stub from the database
        Video video = videoRepository.findByObjectKey(request.getObjectKey())
                .orElseThrow(() -> new ResourceNotFoundException("No pending upload found for key: " + request.getObjectKey()));

        // Enforce user ownership
        User currentUser = securityContextService.getCurrentUser();
        if (!video.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("You do not own this upload request.");
        }

        // Check if already uploaded to prevent redundant processing or overwrite
        if (video.getUploadStatus() != UploadStatus.PENDING_UPLOAD) {
            throw new IllegalStateException("Video is already uploaded or is currently processing. Status: " + video.getUploadStatus());
        }

        // 2. Query MinIO to verify the object exists
        boolean exists = storageService.doesObjectExist(request.getObjectKey());
        if (!exists) {
            log.error("Verification failed: objectKey '{}' does not exist in bucket '{}'", request.getObjectKey(), bucketName);
            video.setUploadStatus(UploadStatus.FAILED);
            videoRepository.save(video);
            throw new IllegalStateException("Video file was not uploaded to storage. Please upload the file first.");
        }

        // 3. Retrieve actual metadata from MinIO
        long fileSize = storageService.getObjectSize(request.getObjectKey());
        String contentType = storageService.getObjectContentType(request.getObjectKey());

        // 4. Update and persist the final metadata
        video.setTitle(request.getTitle());
        video.setDescription(request.getDescription());
        video.setFileSize(fileSize);
        video.setContentType(contentType);
        video.setUploadStatus(UploadStatus.UPLOADED);

        Video savedVideo = videoRepository.save(video);
        log.info("Successfully verified and saved video metadata. Database ID: {}", savedVideo.getId());

        return mapToResponse(savedVideo);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VideoResponse> getAllVideos(UploadStatus status, Pageable pageable) {
        log.info("Retrieving all videos with status: '{}', pageable: '{}'", status, pageable);
        User currentUser = securityContextService.getCurrentUser();
        Page<Video> videos;
        if (status != null) {
            videos = videoRepository.findAllByUserIdAndUploadStatus(currentUser.getId(), status, pageable);
        } else {
            videos = videoRepository.findAllByUserId(currentUser.getId(), pageable);
        }
        return videos.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public VideoResponse getVideoById(Long id) {
        log.info("Retrieving video with ID: '{}'", id);
        User currentUser = securityContextService.getCurrentUser();
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found with ID: " + id));
        if (!video.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Video not found with ID: " + id);
        }
        return mapToResponse(video);
    }

    private String getFileExtension(String contentType) {
        if (contentType == null) {
            return "mp4";
        }
        return switch (contentType.toLowerCase()) {
            case "video/webm" -> "webm";
            case "video/ogg" -> "ogv";
            case "video/quicktime" -> "mov";
            case "video/x-msvideo" -> "avi";
            case "video/x-matroska" -> "mkv";
            default -> "mp4";
        };
    }

    private String generateObjectKey(String extension) {
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String uniqueId = UUID.randomUUID().toString();
        return String.format("videos/%s/%s.%s", datePath, uniqueId, extension);
    }

    private VideoResponse mapToResponse(Video video) {
        return VideoResponse.builder()
                .id(video.getId())
                .title(video.getTitle())
                .description(video.getDescription())
                .objectKey(video.getObjectKey())
                .bucket(video.getBucket())
                .contentType(video.getContentType())
                .fileSize(video.getFileSize())
                .uploadStatus(video.getUploadStatus())
                .createdAt(video.getCreatedAt())
                .updatedAt(video.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VideoResponse> getVideosByChannel(Long channelId, UploadStatus status, Pageable pageable) {
        log.info("Retrieving videos for channel ID: '{}' with status: '{}', pageable: '{}'", channelId, status, pageable);
        // Verify channel exists
        channelService.getChannelEntityById(channelId);

        Page<Video> videos;
        if (status != null) {
            videos = videoRepository.findAllByChannelIdAndUploadStatus(channelId, status, pageable);
        } else {
            videos = videoRepository.findAllByChannelId(channelId, pageable);
        }
        return videos.map(this::mapToResponse);
    }
}
