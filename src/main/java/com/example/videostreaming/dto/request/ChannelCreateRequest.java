package com.example.videostreaming.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelCreateRequest {

    @NotBlank(message = "Channel name is required")
    @Size(min = 3, max = 100, message = "Channel name must be between 3 and 100 characters")
    private String name;

    @Size(max = 1000, message = "Channel description cannot exceed 1000 characters")
    private String description;
}
