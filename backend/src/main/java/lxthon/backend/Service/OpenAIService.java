package lxthon.backend.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;

import lxthon.backend.config.OpenAIConfig;

@Service
public class OpenAIService {
    private final OpenAIConfig openAIConfig;
    private final RestTemplate restTemplate;
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    public OpenAIService(OpenAIConfig openAIConfig) {
        this.openAIConfig = openAIConfig;
        this.restTemplate = new RestTemplate();
    }

    public String getChatCompletion(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAIConfig.getApiKey());

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4");
        requestBody.put("messages", new Object[]{message});

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
            OPENAI_API_URL,
            request,
            Map.class
        );

        // Extract the response content from the API response
        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null && responseBody.containsKey("choices")) {
            Object[] choices = (Object[]) responseBody.get("choices");
            if (choices.length > 0) {
                Map<String, Object> choice = (Map<String, Object>) choices[0];
                Map<String, Object> messageResponse = (Map<String, Object>) choice.get("message");
                return (String) messageResponse.get("content");
            }
        }
        return "No response from OpenAI";
    }
} 