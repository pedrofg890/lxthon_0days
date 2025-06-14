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

/**
 * REST controller exposing endpoints for video processing features:
 * <ul>
 *   <li>Retrieve video metadata</li>
 *   <li>Download video</li>
 *   <li>Fetch raw and cleaned transcripts</li>
 *   <li>Generate summaries</li>
 *   <li>Generate quizzes</li>
 * </ul>
 */
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

    /**
     * Constructs the VideoController with all required services.
     *
     * @param youtubeService             service for interacting with YouTube videos
     * @param videoToSpeechService       service for converting video to speech/audio
     * @param transcriptProcessingService service for cleaning and processing transcripts
     * @param summaryGenerator           service for generating summaries from transcripts
     * @param quizGenerator              service for generating quizzes from transcript text
     */
    public VideoController(VideoService youtubeService, VideoToSpeechService videoToSpeechService, @NonNull TranscriptProcessingService transcriptProcessingService, @NonNull SummaryGeneratorService summaryGenerator,  @NonNull QuizGeneratorService quizGenerator) {
        this.youtubeService = youtubeService;
        this.videoToSpeechService = videoToSpeechService;
        this.transcriptProcessingService = transcriptProcessingService;
        this.summaryGenerator = summaryGenerator;
        this.quizGenerator = quizGenerator;
    }

    /**
     * Retrieves metadata about a YouTube video.
     * <p>
     * Returns information such as title, duration, and available formats.
     * </p>
     *
     * @param url the URL of the YouTube video
     * @return a String containing video metadata in JSON or plain text format
     * @throws IOException          if network or parsing fails
     * @throws InterruptedException if the request is interrupted
     */
    @GetMapping("/info")
    public String getVideoInfo(@RequestParam String url) throws IOException, InterruptedException {
        return youtubeService.getVideoInfo(url);
    }

    /**
     * Downloads a YouTube video in the specified format.
     * <p>
     * If no format is provided, defaults to the service’s standard format.
     * </p>
     *
     * @param url    the URL of the YouTube video
     * @param format optional format identifier (e.g., "mp4", "mp3")
     * @return a String path or URL to the downloaded file
     * @throws IOException          if download or saving fails
     * @throws InterruptedException if the download process is interrupted
     */
    @GetMapping("/download")
    public String downloadVideo(
            @RequestParam String url,
            @RequestParam(required = false) String format) throws IOException, InterruptedException {
        return youtubeService.downloadVideo(url, format);
    }

    /**
     * Fetches the raw transcript segments from a YouTube video.
     * <p>
     * Returns a list of {@link TranscriptSegment}, each with start/end times and text.
     * </p>
     *
     * @param url the URL of the YouTube video
     * @return a ResponseEntity containing the list of segments or an error status
     * @throws IOException          if fetching fails
     * @throws InterruptedException if the operation is interrupted
     */
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

    /**
     * Asynchronously cleans and normalizes transcript segments.
     * <p>
     * Removes filler words, restores punctuation, and preserves timecodes.
     * </p>
     *
     * @param url the URL of the YouTube video
     * @return a CompletableFuture yielding a ResponseEntity with cleaned segments
     * @throws IOException          if processing setup fails
     * @throws InterruptedException if the operation is interrupted
     */
    @GetMapping("/clean-transcript")
    public CompletableFuture<ResponseEntity<List<TranscriptSegment>>> cleanTranscript(@RequestParam String url) throws IOException, InterruptedException {
        return transcriptProcessingService.getCleanedTranscript(url)
                .thenApply(cleanedTranscript -> ResponseEntity.ok(cleanedTranscript))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                });
    }

    /**
     * Generates a summary of the video transcript.
     * <p>
     * Produces a concise text summary based on the full transcript.
     * </p>
     *
     * @param url the URL of the YouTube video
     * @return a ResponseEntity containing the summary text or an error status
     * @throws IOException          if retrieval or summarization fails
     * @throws InterruptedException if the process is interrupted
     */
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

    /**
     * Generates a multiple-choice quiz from the video transcript.
     * <p>
     * First cleans the transcript, then creates a quiz with the specified number
     * of questions using the {@link QuizGeneratorService}. Returns the quiz as JSON.
     * </p>
     *
     * @param url          the URL of the YouTube video
     * @param numQuestions the number of quiz questions to generate (default 5)
     * @return a ResponseEntity with the {@link Quiz} object or an error message
     */
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


