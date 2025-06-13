package lxthon.backend.Service.PodcastGeneration;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


@Service
public class VideoToSpeechService {

    private static final Logger log = LoggerFactory.getLogger(VideoToSpeechService.class);

    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    // Voice IDs for different speakers (you can change these)
    private static final String VOICE_ID_HOST_A = "21m00Tcm4TlvDq8ikWAM"; // Rachel
    private static final String VOICE_ID_HOST_B = "AZnzlk1XvdvUeBnXmlld"; // Domi

    public VideoToSpeechService() {
        // Load API key safely
        Dotenv dotenv = null;
        try {
            dotenv = Dotenv.configure().ignoreIfMissing().load();
        } catch (Exception e) {
            log.warn("Could not load .env file: {}", e.getMessage());
        }

        String key = null;
        if (dotenv != null) {
            key = dotenv.get("ELEVEN_LABS_API_KEY");
        }
        if (key == null || key.trim().isEmpty()) {
            key = System.getenv("ELEVEN_LABS_API_KEY");
        }

        this.apiKey = key;

        this.httpClient = HttpClient.newBuilder().build();

        this.objectMapper = new ObjectMapper();

        if (this.apiKey == null || this.apiKey.trim().isEmpty()) {
            log.warn("ElevenLabs API key not found. Service will run in mock mode.");
        } else {
            log.info("ElevenLabs service initialized successfully");
        }
    }

    /**
     * Generate speech from text using a specific voice
     */
    public byte[] generateSpeech(String text, String voiceId) throws IOException, InterruptedException {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("No API key available, returning mock audio");
            return generateMockAudio(text);
        }

        try {
            String requestBody = String.format("""
                {
                    "text": "%s",
                    "model_id": "eleven_monolingual_v1",
                    "voice_settings": {
                        "stability": 0.5,
                        "similarity_boost": 0.5
                    }
                }
                """, text.replace("\"", "\\\"").replace("\n", "\\n"));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.elevenlabs.io/v1/video-to-speech/" + voiceId))
                    .header("Accept", "audio/mpeg")
                    .header("Content-Type", "application/json")
                    .header("xi-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() != 200) {
                throw new RuntimeException("ElevenLabs API call failed: " + response.statusCode() +
                        " - " + new String(response.body()));
            }

            log.debug("Generated audio for text: {} characters", text.length());
            return response.body();

        } catch (Exception e) {
            log.error("Error calling ElevenLabs API: {}", e.getMessage());
            log.warn("Falling back to mock audio");
            return generateMockAudio(text);
        }
    }

    /**
     * Generate speech for Host A (Rachel voice)
     */
    public byte[] generateHostASpeech(String text) throws IOException, InterruptedException {
        return generateSpeech(text, VOICE_ID_HOST_A);
    }

    /**
     * Generate speech for Host B (Domi voice)
     */
    public byte[] generateHostBSpeech(String text) throws IOException, InterruptedException {
        return generateSpeech(text, VOICE_ID_HOST_B);
    }

    /**
     * Get available voices from ElevenLabs
     */
    public String getAvailableVoices() throws IOException, InterruptedException {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return "Mock voices: Rachel, Domi";
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.elevenlabs.io/v1/voices"))
                    .header("Accept", "application/json")
                    .header("xi-api-key", apiKey)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to get voices: " + response.statusCode());
            }

            return response.body();

        } catch (Exception e) {
            log.error("Error getting voices: {}", e.getMessage());
            return "Error getting voices: " + e.getMessage();
        }
    }

    /**
     * Generate mock audio for development
     */
    private byte[] generateMockAudio(String text) {
        // Return a small mock MP3 header (won't play but won't crash)
        log.warn("Generating mock audio for: {}", text.substring(0, Math.min(50, text.length())));
        return new byte[]{
                (byte)0xFF, (byte)0xFB, (byte)0x90, (byte)0x00, // MP3 header
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
        };
    }

    /**
     * Check if the service has a valid API key
     */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }
}