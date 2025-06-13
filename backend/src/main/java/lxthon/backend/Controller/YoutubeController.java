package lxthon.backend.Controller;

import java.io.IOException;
import java.util.List;

import lxthon.backend.Service.TextToSpeechService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lxthon.backend.Domain.TranscriptSegment;
import lxthon.backend.Service.YoutubeService;

import static java.awt.SystemColor.text;

@RestController
@RequestMapping("/api/videos")
public class YoutubeController {

    private final YoutubeService youtubeService;
    private final TextToSpeechService textToSpeechService;

    public YoutubeController(YoutubeService youtubeService, TextToSpeechService textToSpeechService) {
        this.youtubeService = youtubeService;
        this.textToSpeechService = textToSpeechService;
    }

    @GetMapping("/info")
    public String getVideoInfo(@RequestParam String url) throws IOException, InterruptedException {
        return youtubeService.getVideoInfo(url);
    }

    @GetMapping("/download")
    public String downloadVideo(
            @RequestParam String url,
            @RequestParam(required = false) String format) throws IOException, InterruptedException {
        return youtubeService.downloadVideo(url, format);
    }

    @GetMapping("/transcript")
    public ResponseEntity<List<TranscriptSegment>> getTranscript(@RequestParam String url) throws IOException, InterruptedException {
        try {
            List<TranscriptSegment> transcript = youtubeService.getTranscript(url);
            System.out.println(transcript);
            return ResponseEntity.ok(transcript);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(value = "/synthesize", produces = "audio/mpeg")
    public ResponseEntity<byte[]> synthesize(@RequestBody String text) {
        try {
            byte[] audioBytes = textToSpeechService.synthesizeText(text);

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"output.mp3\"")
                    .contentType(MediaType.valueOf("audio/mpeg"))
                    .body(audioBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
}


