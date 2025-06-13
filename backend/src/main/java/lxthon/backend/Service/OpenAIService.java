package lxthon.backend.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import lxthon.backend.config.OpenAIConfig;

@Service
public class OpenAIService {

    private final OpenAIConfig cfg;
    private final RestTemplate restTemplate;

    public OpenAIService(OpenAIConfig cfg) {
        this.cfg = cfg;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Envia um prompt ao OpenAI e devolve o conteúdo da resposta.
     */
    public String getChatCompletion(String prompt) {
        // 1) Prepara headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(cfg.getApiKey());

        // 2) Mensagem de usuário
        Map<String,Object> message = Map.of(
                "role", "user",
                "content", prompt
        );

        // 3) Corpo da requisição
        Map<String,Object> body = Map.of(
                "model", cfg.getModel(),
                "messages", List.of(message),
                "temperature", 0.0
        );

        HttpEntity<Map<String,Object>> request = new HttpEntity<>(body, headers);

        // 4) POST para o endpoint configurado
        ResponseEntity<Map> response = restTemplate.postForEntity(
                cfg.getEndpoint(),
                request,
                Map.class
        );

        // 5) Extrai texto da primeira escolha
        Map<String,Object> respBody = response.getBody();
        if (respBody != null && respBody.containsKey("choices")) {
            @SuppressWarnings("unchecked")
            List<Map<String,Object>> choices = (List<Map<String,Object>>) respBody.get("choices");
            if (!choices.isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String,Object> first = choices.get(0);
                @SuppressWarnings("unchecked")
                Map<String,Object> messageResp = (Map<String,Object>) first.get("message");
                return (String) messageResp.get("content");
            }
        }
        return "No response from OpenAI";
    }
}
