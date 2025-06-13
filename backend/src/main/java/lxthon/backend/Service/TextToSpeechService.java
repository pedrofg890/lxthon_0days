package lxthon.backend.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Service
public class TextToSpeechService {

    private static final String API_KEY = "YOUR_API_KEY";
    private static final String URL = "https://texttospeech.googleapis.com/v1/text:synthesize?key=" + API_KEY;

    public String synthesizeText(String text) {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> request = new HashMap<>();
        request.put("input", Map.of("text", text));
        request.put("voice", Map.of("languageCode", "en-US", "ssmlGender", "NEUTRAL"));
        request.put("audioConfig", Map.of("audioEncoding", "MP3"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        Map response = restTemplate.postForObject(URL, entity, Map.class);

        return (String) response.get("audioContent"); // Base64-encoded MP3
    }
}
