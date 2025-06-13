package lxthon.backend.Controller;

import org.springframework.http.ResponseEntity;
import lombok.NonNull;
import lxthon.backend.Service.PodcastService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lxthon.backend.Service.OpenAIService;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @NonNull
    private final OpenAIService openAIService;

    @NonNull
    private final PodcastService podcastService;

    public ChatController(OpenAIService openAIService, PodcastService podcastService) {
        this.openAIService = openAIService;
        this.podcastService = podcastService;
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