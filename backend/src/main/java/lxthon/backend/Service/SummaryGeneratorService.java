package lxthon.backend.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import lxthon.backend.Domain.TranscriptSegment;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service responsible for generating a concise summary from a list of transcript segments.
 * <p>
 * It concatenates the raw transcript text, sends it to the language model with a prompt
 * instructing it to produce only the summary text, and returns the trimmed result.
 * </p>
 */
@Service
public class SummaryGeneratorService {      
    private final OpenAIService openAIService;
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * The system prompt used to instruct the model to produce a summary.
     * <p>
     * Emphasizes clarity, structure, and conciseness, and requests only the summary text
     * without additional formatting or explanation.
     * </p>
     */
    private static final String SYSTEM_PROMPT = 
        "You are a professional content summarizer. Given a transcript text, " +
        "create a comprehensive summary that captures the main points and key ideas. " +
        "The summary should be well-structured, clear, and maintain the original context. " +
        "Focus on the most important information while being concise. " +
        "Return ONLY the summary text, without any additional formatting or explanations.";


    /**
     * Constructs a new {@code SummaryGeneratorService} using the provided {@link OpenAIService}.
     *
     * @param openAIService the service used to send prompts and receive completions
     */
    public SummaryGeneratorService(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    /**
     * Generates a text summary based on the list of transcript segments.
     * <p>
     * It extracts the original transcript text from each segment, joins them
     * into a single string, and sends the combined text to the AI service
     * with the summarization prompt. Returns the resulting summary.
     * </p>
     *
     * @param segments the list of {@link TranscriptSegment} to summarize
     * @return a concise summary string produced by the AI
     * @throws IOException if an error occurs during prompt processing or response handling
     */
    public String generateSummary(List<TranscriptSegment> segments) throws IOException {
        // Extract all normalizedText segments and join them into a single string
        String fullTranscript = segments.stream()
            .map(TranscriptSegment::getText)
            .collect(Collectors.joining("\n"));
        
        // Create the prompt with the full transcript
        String prompt = SYSTEM_PROMPT + "\n\nTranscript:\n" + fullTranscript;
        
        // Get the summary from the AI service
        return openAIService.getChatCompletion(prompt).trim();
    }
} 