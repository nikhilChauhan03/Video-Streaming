package com.example.videostreaming.service;

import com.example.videostreaming.dto.request.ChannelCreateRequest;
import com.example.videostreaming.dto.response.ChannelResponse;
import com.example.videostreaming.entity.Channel;

public interface ChannelService {
    ChannelResponse createChannel(ChannelCreateRequest request);
    ChannelResponse getChannelByCurrentUser();
    ChannelResponse getChannelById(Long id);
    Channel getChannelEntityById(Long id);
}
