package lxthon.backend.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import lxthon.backend.Domain.TranscriptSegment;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SummaryGeneratorService {      
    private final OpenAIService openAIService;
    private final ObjectMapper mapper = new ObjectMapper();
    
    private static final String SYSTEM_PROMPT = 
        "You are a professional content summarizer. Given a transcript text, " +
        "create a comprehensive summary that captures the main points and key ideas. " +
        "The summary should be well-structured, clear, and maintain the original context. " +
        "Focus on the most important information while being concise. " +
        "Return ONLY the summary text, without any additional formatting or explanations.";

    public SummaryGeneratorService(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

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