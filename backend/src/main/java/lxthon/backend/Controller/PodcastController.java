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

    /**
     * Constructs a new {@code PodcastController} with the required services.
     *
     * @param openAIService   the service used to interact with the OpenAI API
     * @param podcastService  the service responsible for generating podcast scripts and audio
     */
    public PodcastController(@NotNull OpenAIService openAIService, @NonNull PodcastService podcastService) {
        this.openAIService = openAIService;
        this.podcastService = podcastService;
    }

    /**
     * Returns a raw chat completion from the OpenAI service for the given prompt.
     * <p>
     * This endpoint can be used to forward any prompt string directly to OpenAI
     * and retrieve the model's completion text.
     * </p>
     *
     * @param prompt the user-provided prompt to send to OpenAI
     * @return the raw completion text returned by the OpenAI API
     */
    @PostMapping("/completion")
    public String getChatCompletion(@RequestBody String prompt) {
        return openAIService.getChatCompletion(prompt);
    }

    /**
     * Tests connectivity to the OpenAI API by sending a simple prompt.
     * <p>
     * If successful, returns the model's response; otherwise returns a 400 status
     * with the error message.
     * </p>
     *
     * @return a {@code ResponseEntity} containing either the successful response or an error message
     */
    @GetMapping("/test-openai")
    public ResponseEntity<String> testOpenAI() {
        try {
            String response = openAIService.getChatCompletion("Say 'Hello World' if you can hear me.");
            return ResponseEntity.ok("OpenAI Response: " + response);
        } catch (Exception e) {
            return ResponseEntity.status(400).body("OpenAI Error: " + e.getMessage());
        }
    }

    /**
     * Generates a podcast script and audio based on a YouTube video URL.
     * <p>
     * Accepts the video URL and optional host names for two hosts, then delegates
     * to {@link PodcastService} to produce the script and audio file.
     * Returns a JSON object detailing success, script text, host names,
     * audio file size, and a status message.
     * </p>
     *
     * @param url      the YouTube video URL to base the podcast on
     * @param hostA    the name of the first host (default: "Sofia")
     * @param hostB    the name of the second host (default: "Miguel")
     * @return a {@code ResponseEntity} containing a map with:
     *         <ul>
     *           <li>success (boolean)</li>
     *           <li>script (String)</li>
     *           <li>hostAName (String)</li>
     *           <li>hostBName (String)</li>
     *           <li>audioSizeBytes (long)</li>
     *           <li>message (String)</li>
     *         </ul>
     *         or an error map with success=false and an error message on failure.
     */
    @PostMapping("/generate-podcast")
    public ResponseEntity<Map<String, Object>> generatePodcast(@RequestParam String url,
                                                               @RequestParam(required = false, defaultValue = "Sofia") String hostA,
                                                               @RequestParam(required = false, defaultValue = "Miguel") String hostB) {
        try {
            log.info("Received podcast generation request for URL: {}", url);
            PodcastService.PodcastResult result = podcastService.generatePodcastFromVideo(url, hostA, hostB);
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