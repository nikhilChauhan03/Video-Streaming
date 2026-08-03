package com.example.videostreaming.controller;

import com.example.videostreaming.dto.request.ChannelCreateRequest;
import com.example.videostreaming.dto.response.ApiResponse;
import com.example.videostreaming.dto.response.ChannelResponse;
import com.example.videostreaming.dto.response.VideoResponse;
import com.example.videostreaming.entity.UploadStatus;
import com.example.videostreaming.service.ChannelService;
import com.example.videostreaming.service.VideoService;
import jakarta.validation.Valid;
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

@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ChannelController {

    private final ChannelService channelService;
    private final VideoService videoService;

    /**
     * Creates a new channel for the currently authenticated user.
     * Exposes POST /api/channels
     *
     * @param request payload containing name and description
     * @return created channel details
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ChannelResponse> createChannel(@Valid @RequestBody ChannelCreateRequest request) {
        log.info("Received request to create channel with name: '{}'", request.getName());
        ChannelResponse response = channelService.createChannel(request);
        return ApiResponse.success("Channel created successfully", response);
    }

    /**
     * Retrieves the channel owned by the currently authenticated user.
     * Exposes GET /api/channels/my
     *
     * @return current user's channel details
     */
    @GetMapping("/my")
    public ApiResponse<ChannelResponse> getMyChannel() {
        log.info("Received request to get channel details for current authenticated user");
        ChannelResponse response = channelService.getChannelByCurrentUser();
        return ApiResponse.success("Channel details retrieved successfully", response);
    }

    /**
     * Retrieves a paginated list of videos belonging to a specific channel.
     * Exposes GET /api/channels/{channelId}/videos
     *
     * @param channelId the target channel ID
     * @param status optional status to filter by
     * @param page page index (0-based)
     * @param size page size
     * @param sortBy property to sort by
     * @param direction sort direction (ASC or DESC)
     * @return a paginated wrapper containing list of video metadata
     */
    @GetMapping("/{channelId}/videos")
    public ApiResponse<Page<VideoResponse>> getVideosByChannel(
            @PathVariable Long channelId,
            @RequestParam(required = false) UploadStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        log.info("Received request to list videos for channel: '{}'. status: '{}', page: '{}', size: '{}', sortBy: '{}', direction: '{}'",
                channelId, status, page, size, sortBy, direction);

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<VideoResponse> response = videoService.getVideosByChannel(channelId, status, pageRequest);
        return ApiResponse.success("Channel videos retrieved successfully", response);
    }
}
