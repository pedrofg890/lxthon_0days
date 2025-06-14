package lxthon.backend.Controller;

import lxthon.backend.Service.PodcastGeneration.PodcastService;
import lxthon.backend.Service.PodcastGeneration.VideoToSpeechService;
import lxthon.backend.Service.VideoService;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import java.io.File;
import java.io.FileOutputStream;
import org.springframework.http.ResponseEntity;
import lombok.NonNull;
import org.springframework.web.bind.annotation.*;
import lxthon.backend.Service.OpenAIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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

    @NonNull
    private final VideoService videoService;

    private final Map<String, PodcastService.PodcastResult> podcastCache = new ConcurrentHashMap<>();

    /**
     * Constructs a new {@code PodcastController} with the required services.
     *
     * @param openAIService   the service used to interact with the OpenAI API
     * @param podcastService  the service responsible for generating podcast scripts and audio
     */
    public PodcastController(@NotNull OpenAIService openAIService, @NonNull PodcastService podcastService, @NonNull VideoService videoService) {
        this.openAIService = openAIService;
        this.podcastService = podcastService;
        this.videoService = videoService;
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
     *
     * Metodo de teste para falar com eleven labs AI
     *
     */

    @PostMapping("/test-save-audio")
    public ResponseEntity<Map<String, Object>> testAndSaveAudio(@RequestParam String text) {
        try {
            log.info("Testing and saving audio for text: {}", text.substring(0, Math.min(50, text.length())));

            VideoToSpeechService speechService = new VideoToSpeechService();
            byte[] audio = speechService.generateHostASpeech(text);

            // Guardar o ficheiro localmente
            String filename = "test_audio_" + System.currentTimeMillis() + ".mp3";
            String filepath = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + filename;

            try (FileOutputStream fos = new FileOutputStream(filepath)) {
                fos.write(audio);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("audioSizeBytes", audio.length);
            response.put("savedTo", filepath);
            response.put("message", "Audio saved to desktop - check file: " + filename);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error testing and saving audio", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
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
    public ResponseEntity<Map<String, Object>> generatePodcast(@RequestParam String url) {
        try {
            log.info("Received podcast generation request for URL: {}", url);

            // Gerar o podcast
            PodcastService.PodcastResult result = podcastService.generatePodcastFromVideo(url, "Ana", "João");

            // Criar ID único e guardar no cache
            String podcastId = UUID.randomUUID().toString();
            podcastCache.put(podcastId, result);

            log.info("Podcast generated successfully. ID: {}, Script: {} chars, Audio: {} bytes",
                    podcastId, result.getScript().length(), result.getAudioSizeBytes());

            // Criar resposta com todas as opções
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("podcastId", podcastId);
            response.put("videoUrl", url);
            response.put("script", result.getScript());
            response.put("hosts", "Ana & João");
            response.put("audioSizeBytes", result.getAudioSizeBytes());
            response.put("downloadUrl", "/podcast-api/chat/download/" + podcastId);
            response.put("streamUrl", "/podcast-api/chat/stream/" + podcastId);
            response.put("message", "Podcast generated successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error generating podcast for URL: {}", url, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("videoUrl", url);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     Creates the download functionality of the podcast.
     It downloads the audio of the podcast to the user's machine if they select "Donwload Podcast"
     */

    @GetMapping("/download/{podcastId}")
    public ResponseEntity<byte[]> downloadPodcast(@PathVariable String podcastId) {
        PodcastService.PodcastResult result = podcastCache.get(podcastId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"podcast.mp3\"")
                .header("Content-Type", "audio/mpeg")
                .body(result.getAudio());
    }

    /**
     Allows the user to play the podcast on the web interface.
     It plays the podcast if they select "Stream Podcast"
     */

    @GetMapping("/stream/{podcastId}")
    public ResponseEntity<byte[]> streamPodcast(@PathVariable String podcastId) {
        PodcastService.PodcastResult result = podcastCache.get(podcastId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header("Content-Type", "audio/mpeg")
                .header("Accept-Ranges", "bytes") // Para permitir seek no audio
                .body(result.getAudio());
    }

    /**
     * Endpoint para limpar cache (útil para testes)
     */
    @PostMapping("/clear-cache")
    public ResponseEntity<Map<String, Object>> clearCache() {
        int sizeBefore = podcastCache.size();
        podcastCache.clear();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Cache cleared");
        response.put("itemsRemoved", sizeBefore);

        log.info("Podcast cache cleared. Removed {} items", sizeBefore);
        return ResponseEntity.ok(response);
    }
}