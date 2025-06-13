package lxthon.backend.Controller;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import lombok.NonNull;
import lxthon.backend.Domain.Quiz;
import lxthon.backend.Service.*;
import lxthon.backend.Service.PodcastGeneration.VideoToSpeechService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lxthon.backend.Domain.TranscriptSegment;


@RestController
@RequestMapping("/api/videos")
public class VideoController {

    @NonNull
    private final VideoService youtubeService;

    @NonNull
    private final VideoToSpeechService videoToSpeechService;

    @NonNull
    private final TranscriptProcessingService transcriptProcessingService;

    @NonNull
    private final SummaryGeneratorService summaryGenerator;

    @NonNull
    private final QuizGeneratorService quizGenerator;

    public VideoController(VideoService youtubeService, VideoToSpeechService videoToSpeechService, @NonNull TranscriptProcessingService transcriptProcessingService, @NonNull SummaryGeneratorService summaryGenerator,  @NonNull QuizGeneratorService quizGenerator) {
        this.youtubeService = youtubeService;
        this.videoToSpeechService = videoToSpeechService;
        this.transcriptProcessingService = transcriptProcessingService;
        this.summaryGenerator = summaryGenerator;
        this.quizGenerator = quizGenerator;
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

    @GetMapping("/clean-transcript")
    public CompletableFuture<ResponseEntity<List<TranscriptSegment>>> cleanTranscript(@RequestParam String url) throws IOException, InterruptedException {
        return transcriptProcessingService.getCleanedTranscript(url)
                .thenApply(cleanedTranscript -> ResponseEntity.ok(cleanedTranscript))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                });
    }

    @GetMapping("/summary")
    public ResponseEntity<String> getSummary(@RequestParam String url) throws IOException, InterruptedException {
        try {
            List<TranscriptSegment> transcript = youtubeService.getTranscript(url);
            String summary = summaryGenerator.generateSummary(transcript);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/quiz")
    public ResponseEntity<?> generateQuiz (@RequestParam String url, @RequestParam(defaultValue = "5") int numQuestions) {
        try {
            List<TranscriptSegment> transcript = youtubeService.getTranscript(url);
            String summary = summaryGenerator.generateSummary(transcript);
            Quiz quiz = quizGenerator.generateQuiz(summary, numQuestions);

            return ResponseEntity.ok(quiz);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao gerar quiz.");
        }
    }

}


