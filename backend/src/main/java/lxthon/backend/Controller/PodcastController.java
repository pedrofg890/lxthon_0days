package lxthon.backend.Controller;

import lxthon.backend.Service.PodcastGeneration.PodcastService;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import lombok.NonNull;
import org.springframework.web.bind.annotation.*;
import lxthon.backend.Service.OpenAIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/podcast-api/chat")
public class PodcastController {

    private static final Logger log = LoggerFactory.getLogger(PodcastController.class);

    @NonNull
    private final OpenAIService openAIService;

    @NonNull
    private final PodcastService podcastService;

    public PodcastController(@NotNull OpenAIService openAIService, @NonNull PodcastService podcastService) {
        this.openAIService = openAIService;
        this.podcastService = podcastService;
    }

    @PostMapping("/completion")
    public String getChatCompletion(@RequestBody String prompt) {
        return openAIService.getChatCompletion(prompt);
    }

    // for testing the OPEN-AI API
    @GetMapping("/test-openai")
    public ResponseEntity<String> testOpenAI() {
        try {
            String response = openAIService.getChatCompletion("Say 'Hello World' if you can hear me.");
            return ResponseEntity.ok("OpenAI Response: " + response);
        } catch (Exception e) {
            return ResponseEntity.status(400).body("OpenAI Error: " + e.getMessage());
        }
    }

    @PostMapping("/generate-podcast")
    public ResponseEntity<Map<String, Object>> generatePodcast(@RequestBody PodcastRequest request) {
        try {
            log.info("Received podcast generation request for URL: {}", request.getVideoUrl());

            // Generate the podcast
            PodcastService.PodcastResult result = podcastService.generatePodcastFromVideo(
                    request.getVideoUrl(),
                    request.getHostAName() != null ? request.getHostAName() : "Alex",
                    request.getHostBName() != null ? request.getHostBName() : "Sam"
            );

            // Create response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("script", result.getScript());
            response.put("hostAName", result.getHostAName());
            response.put("hostBName", result.getHostBName());
            response.put("audioSizeBytes", result.getAudioSizeBytes());
            response.put("message", "Podcast generated successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error generating podcast", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/generate-podcast-audio")
    public ResponseEntity<byte[]> generatePodcastAudio(@RequestBody PodcastRequest request) {
        try {
            log.info("Generating podcast audio for URL: {}", request.getVideoUrl());

            PodcastService.PodcastResult result = podcastService.generatePodcastFromVideo(
                    request.getVideoUrl(),
                    request.getHostAName() != null ? request.getHostAName() : "Alex",
                    request.getHostBName() != null ? request.getHostBName() : "Sam"
            );

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"podcast.mp3\"")
                    .contentType(MediaType.valueOf("audio/mpeg"))
                    .body(result.getAudio());

        } catch (Exception e) {
            log.error("Error generating podcast audio", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // DTO para o request
    public static class PodcastRequest {
        private String videoUrl;
        private String hostAName;
        private String hostBName;

        // Getters e setters
        public String getVideoUrl() { return videoUrl; }
        public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

        public String getHostAName() { return hostAName; }
        public void setHostAName(String hostAName) { this.hostAName = hostAName; }

        public String getHostBName() { return hostBName; }
        public void setHostBName(String hostBName) { this.hostBName = hostBName; }
    }
} 