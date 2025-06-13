package lxthon.backend.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Service
public class TextToSpeechService {

    private static final String API_KEY = "sk_6ad67e89486ad30915582bd90a0dc3dcc6b0eb2bac444b7f";
    private static final String VOICE_ID = "Xb7hH8MSUJpSbSDYk0k2";
    private static final String URL = "https://api.elevenlabs.io/v1/text-to-speech/" + VOICE_ID + "/stream";

    public byte[] synthesizeText(String text) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("xi-api-key", API_KEY);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("text", text);
        requestBody.put("voice_settings", Map.of("stability", 0, "similarity_boost", 0));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<byte[]> response = restTemplate.exchange(
                URL,
                HttpMethod.POST,
                entity,
                byte[].class
        );

        return response.getBody();
    }
}
