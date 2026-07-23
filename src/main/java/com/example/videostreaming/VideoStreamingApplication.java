package com.example.videostreaming;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Video Streaming Application.
 * This class boots the Spring application context, initializing all configurations and starters.
 */
@SpringBootApplication
public class VideoStreamingApplication {

    public static void main(String[] args) {
        SpringApplication.run(VideoStreamingApplication.class, args);
    }
}
