package lxthon.backend.Service;

import org.springframework.stereotype.Service;
import lxthon.backend.Domain.TranscriptSegment;
import java.io.IOException;
import java.util.List;

/**
 * TranscriptProcessingService: orchestrates transcript extraction and cleaning.
 * This service acts as a facade to coordinate YoutubeService and TranscriptCleanerService.
 */
@Service
public class TranscriptProcessingService {
    
    private final YoutubeService youtubeService;
    private final TranscriptCleanerService transcriptCleanerService;
    
    public TranscriptProcessingService(YoutubeService youtubeService, 
                                     TranscriptCleanerService transcriptCleanerService) {
        this.youtubeService = youtubeService;
        this.transcriptCleanerService = transcriptCleanerService;
    }
    
    /**
     * Extracts and cleans transcript from a YouTube video
     * @param url YouTube video URL
     * @return List of cleaned TranscriptSegment objects with both text and normalizedText populated
     */
    public List<TranscriptSegment> getCleanedTranscript(String url) throws IOException, InterruptedException {        
        // Step 1: Extract raw transcript from YouTube
        List<TranscriptSegment> rawTranscript = youtubeService.getTranscript(url);
        
        // Step 2: Clean the transcript using AI
        List<TranscriptSegment> cleanedTranscript = transcriptCleanerService.cleanTranscript(rawTranscript);
        
        return cleanedTranscript;
    }
    
    /**
     * Get raw transcript only (without cleaning)
     * @param url YouTube video URL
     * @return List of raw TranscriptSegment objects with only text populated
     */
    public List<TranscriptSegment> getRawTranscript(String url) throws IOException, InterruptedException {
        return youtubeService.getTranscript(url);
    }
} 