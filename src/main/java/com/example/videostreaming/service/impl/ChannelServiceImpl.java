package com.example.videostreaming.service.impl;

import com.example.videostreaming.dto.request.ChannelCreateRequest;
import com.example.videostreaming.dto.response.ChannelResponse;
import com.example.videostreaming.entity.Channel;
import com.example.videostreaming.entity.User;
import com.example.videostreaming.exception.ResourceNotFoundException;
import com.example.videostreaming.repository.ChannelRepository;
import com.example.videostreaming.security.SecurityContextService;
import com.example.videostreaming.service.ChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChannelServiceImpl implements ChannelService {

    private final ChannelRepository channelRepository;
    private final SecurityContextService securityContextService;

    @Override
    @Transactional
    public ChannelResponse createChannel(ChannelCreateRequest request) {
        log.info("Request to create channel with name: '{}'", request.getName());

        User currentUser = securityContextService.getCurrentUser();

        // 1. One user can only have one channel constraint
        if (channelRepository.existsByOwnerId(currentUser.getId())) {
            log.error("User '{}' already has a channel.", currentUser.getUsername());
            throw new IllegalStateException("User already has a channel. Only one channel is allowed per user.");
        }

        // 2. Channel name must be globally unique
        if (channelRepository.existsByName(request.getName())) {
            log.error("Channel name '{}' is already taken.", request.getName());
            throw new IllegalArgumentException("Channel name '" + request.getName() + "' is already taken.");
        }

        Channel channel = Channel.builder()
                .name(request.getName())
                .description(request.getDescription())
                .owner(currentUser)
                .build();

        Channel savedChannel = channelRepository.save(channel);
        log.info("Successfully created channel. ID: {}, Name: {}", savedChannel.getId(), savedChannel.getName());

        return mapToResponse(savedChannel);
    }

    @Override
    @Transactional(readOnly = true)
    public ChannelResponse getChannelByCurrentUser() {
        User currentUser = securityContextService.getCurrentUser();
        log.info("Fetching channel for user: '{}'", currentUser.getUsername());

        Channel channel = channelRepository.findByOwnerId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Channel not found for current user. Please create one."));

        return mapToResponse(channel);
    }

    @Override
    @Transactional(readOnly = true)
    public ChannelResponse getChannelById(Long id) {
        log.info("Fetching channel with ID: '{}'", id);
        return mapToResponse(getChannelEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Channel getChannelEntityById(Long id) {
        return channelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Channel not found with ID: " + id));
    }

    private ChannelResponse mapToResponse(Channel channel) {
        return ChannelResponse.builder()
                .id(channel.getId())
                .name(channel.getName())
                .description(channel.getDescription())
                .ownerId(channel.getOwner().getId())
                .createdAt(channel.getCreatedAt())
                .updatedAt(channel.getUpdatedAt())
                .build();
    }
}
