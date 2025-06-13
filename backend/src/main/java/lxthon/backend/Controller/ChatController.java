package lxthon.backend.Controller;

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
} 