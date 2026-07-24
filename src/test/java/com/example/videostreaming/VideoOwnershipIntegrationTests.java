package com.example.videostreaming;

import com.example.videostreaming.dto.request.SignupRequest;
import com.example.videostreaming.dto.request.SigninRequest;
import com.example.videostreaming.entity.UploadStatus;
import com.example.videostreaming.entity.User;
import com.example.videostreaming.entity.Video;
import com.example.videostreaming.repository.UserRepository;
import com.example.videostreaming.repository.VideoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
public class VideoOwnershipIntegrationTests {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testVideoOwnershipIsolation() throws Exception {
        // 1. Register and sign in Alice
        String aliceUser = "alice_" + System.currentTimeMillis();
        SignupRequest aliceSignup = SignupRequest.builder()
                .username(aliceUser)
                .email(aliceUser + "@example.com")
                .password("password123")
                .build();
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(aliceSignup)))
                .andExpect(status().isCreated());

        MvcResult aliceResult = mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new SigninRequest(aliceUser, "password123"))))
                .andExpect(status().isOk())
                .andReturn();
        String aliceToken = objectMapper.readTree(aliceResult.getResponse().getContentAsString())
                .path("data").path("accessToken").asText();

        // 2. Register and sign in Bob
        String bobUser = "bob_" + System.currentTimeMillis();
        SignupRequest bobSignup = SignupRequest.builder()
                .username(bobUser)
                .email(bobUser + "@example.com")
                .password("password123")
                .build();
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bobSignup)))
                .andExpect(status().isCreated());

        MvcResult bobResult = mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new SigninRequest(bobUser, "password123"))))
                .andExpect(status().isOk())
                .andReturn();
        String bobToken = objectMapper.readTree(bobResult.getResponse().getContentAsString())
                .path("data").path("accessToken").asText();

        // 3. Create a video belonging to Alice directly in DB
        User alice = userRepository.findByUsername(aliceUser).orElseThrow();
        Video aliceVideo = Video.builder()
                .title("Alice's Private Video")
                .description("Top Secret")
                .objectKey("videos/alice/private.mp4")
                .bucket("video-streaming")
                .contentType("video/mp4")
                .uploadStatus(UploadStatus.UPLOADED)
                .user(alice)
                .build();
        aliceVideo = videoRepository.save(aliceVideo);

        // 4. Alice requests her videos -> Should see 1 video
        mockMvc.perform(get("/api/videos")
                .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("Alice's Private Video"));

        // 5. Bob requests his videos -> Should see 0 videos
        mockMvc.perform(get("/api/videos")
                .header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(0));

        // 6. Bob tries to get Alice's video by ID -> Should get 404 Not Found
        mockMvc.perform(get("/api/videos/" + aliceVideo.getId())
                .header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isNotFound());

        // 7. Alice gets her video by ID -> Should get 200 OK
        mockMvc.perform(get("/api/videos/" + aliceVideo.getId())
                .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Alice's Private Video"));
    }
}
