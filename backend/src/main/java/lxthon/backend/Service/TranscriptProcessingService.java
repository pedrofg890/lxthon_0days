package lxthon.backend.Service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import lxthon.backend.Domain.TranscriptSegment;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service that orchestrates transcript extraction and cleaning for YouTube videos.
 * <p>
 * Acts as a facade combining {@link VideoService} for retrieval and
 * {@link TranscriptCleanerService} for AI-powered normalization.
 * </p>
 */
@Service
public class TranscriptProcessingService {
    
    private final VideoService youtubeService;
    private final TranscriptCleanerService transcriptCleanerService;

    /**
     * Constructs a new TranscriptProcessingService.
     *
     * @param youtubeService            the service used to fetch raw transcripts
     * @param transcriptCleanerService  the service used to clean and normalize transcripts
     */
    public TranscriptProcessingService(VideoService youtubeService, 
                                     TranscriptCleanerService transcriptCleanerService) {
        this.youtubeService = youtubeService;
        this.transcriptCleanerService = transcriptCleanerService;
    }

    /**
     * Asynchronously extracts the raw transcript from a YouTube video URL and
     * cleans it via the AI-based cleaner.
     * <p>
     * Steps:
     * <ol>
     *   <li>Fetch raw transcript segments from {@link VideoService}.</li>
     *   <li>Clean and normalize them with {@link TranscriptCleanerService}.</li>
     *   <li>Return a {@link CompletableFuture} wrapping the cleaned list.</li>
     * </ol>
     * </p>
     *
     * @param url the YouTube video URL to process
     * @return a CompletableFuture containing the list of cleaned {@link TranscriptSegment} objects
     * @throws IOException          if fetching or parsing the transcript fails
     * @throws InterruptedException if the YouTube retrieval is interrupted
     */
    @Async
    public CompletableFuture<List<TranscriptSegment>> getCleanedTranscript(String url)
            throws IOException, InterruptedException {

        // Step 1: Extract raw transcript from YouTube
        List<TranscriptSegment> rawTranscript = youtubeService.getTranscript(url);

        // Step 2: Clean the transcript using AI
        List<TranscriptSegment> cleanedTranscript = transcriptCleanerService.cleanTranscript(rawTranscript);

        // Step 3: Wrap the result in a CompletableFuture
        return CompletableFuture.completedFuture(cleanedTranscript);
    }

    /**
     * Synchronously fetches the raw transcript segments for a YouTube video URL
     * without applying any cleaning or normalization.
     *
     * @param url the YouTube video URL
     * @return the list of raw {@link TranscriptSegment} objects containing only the original text
     * @throws IOException          if fetching the transcript fails
     * @throws InterruptedException if the retrieval is interrupted
     */
    public List<TranscriptSegment> getRawTranscript (String url) throws IOException, InterruptedException {
        return youtubeService.getTranscript(url);
    }
} 