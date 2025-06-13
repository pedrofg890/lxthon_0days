package lxthon.backend.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lxthon.backend.Service.OpenAIService;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final OpenAIService openAIService;

    public ChatController(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    @PostMapping("/completion")
    public String getChatCompletion(@RequestBody String prompt) {
        return openAIService.getChatCompletion(prompt);
    }

    @GetMapping("/test-openai")
    public ResponseEntity<String> testOpenAI() {
        try {
            String response = openAIService.getChatCompletion("Say 'Hello World' if you can hear me.");
            return ResponseEntity.ok("OpenAI Response: " + response);
        } catch (Exception e) {
            return ResponseEntity.status(400).body("OpenAI Error: " + e.getMessage());
        }
    }
} 