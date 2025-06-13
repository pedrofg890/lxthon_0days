package lxthon.backend.Controller;

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

//    @GetMapping("/podcast")
//    public ResponseEntity<byte[]> generatePodcast(@RequestParam String url) {
//        try {
//            byte[] podcastAudio = podcastService.createPodcastFromYoutube(url);
//            return ResponseEntity.ok()
//                    .header("Content-Disposition", "attachment; filename=\"podcast.mp3\"")
//                    .contentType(MediaType.valueOf("audio/mpeg"))
//                    .body(podcastAudio);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(500).body(null);
//        }
//    }
} 