package com.example.videostreaming;

import com.example.videostreaming.dto.request.SignupRequest;
import com.example.videostreaming.dto.request.SigninRequest;
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

import jakarta.servlet.http.Cookie;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
public class VideoAuthenticationIntegrationTests {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    public void testFullAuthFlowAndEndpointProtection() throws Exception {
        String uniqueUsername = "user_" + System.currentTimeMillis();
        String email = uniqueUsername + "@example.com";
        String password = "securepassword";

        // 1. Sign Up
        SignupRequest signupRequest = SignupRequest.builder()
                .username(uniqueUsername)
                .email(email)
                .password(password)
                .build();

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        // 2. Sign In
        SigninRequest signinRequest = SigninRequest.builder()
                .username(uniqueUsername)
                .password(password)
                .build();

        MvcResult signinResult = mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signinRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(cookie().exists("refreshToken"))
                .andReturn();

        String responseBody = signinResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseBody).path("data").path("accessToken").asText();
        Cookie refreshCookie = signinResult.getResponse().getCookie("refreshToken");
        assertNotNull(refreshCookie);

        // 3. Access Secured Videos Endpoint Without Token -> Expect 403 Forbidden
        mockMvc.perform(get("/api/videos"))
                .andExpect(status().isForbidden());

        // 4. Access Secured Videos Endpoint With Token -> Expect 200 OK
        mockMvc.perform(get("/api/videos")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 5. Refresh Token
        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                .cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(cookie().exists("refreshToken"))
                .andReturn();

        Cookie newRefreshCookie = refreshResult.getResponse().getCookie("refreshToken");
        assertNotNull(newRefreshCookie);

        // 6. Log Out
        mockMvc.perform(post("/api/auth/logout")
                .cookie(newRefreshCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(cookie().maxAge("refreshToken", 0));
    }
}
