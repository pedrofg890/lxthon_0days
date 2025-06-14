package lxthon.backend.Controller;

import lxthon.backend.Service.PodcastGeneration.PodcastService;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import lombok.NonNull;
import org.springframework.web.bind.annotation.*;
import lxthon.backend.Service.OpenAIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for handling podcast-related operations.
 * <p>
 * Exposes endpoints for testing the OpenAI integration, obtaining chat completions,
 * and generating a podcast script and audio based on a YouTube video URL and host names.
 * </p>
 */
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
    public ResponseEntity<Map<String, Object>> generatePodcast(@RequestParam String url,
                                                               @RequestParam(required = false, defaultValue = "Sofia") String hostA,
                                                               @RequestParam(required = false, defaultValue = "Miguel") String hostB) {
        try {
            log.info("Received podcast generation request for URL: {}", url);

            // Generate the podcast
            PodcastService.PodcastResult result = podcastService.generatePodcastFromVideo(url, hostA, hostB);

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
}