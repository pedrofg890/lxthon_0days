package lxthon.backend.Service;

import lombok.NonNull;
import org.springframework.stereotype.Service;
import lxthon.backend.Domain.TranscriptSegment;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * TranscriptCleanerService: limpa e normaliza segmentos de transcrição via OpenAIService.
 */
@Service
public class TranscriptCleanerService {

    @NonNull
    private final OpenAIService openAIService;

    @NonNull
    private static final String SYSTEM_PROMPT =
            "You are a deterministic, context-aware transcript cleaner. " +
                    "Given an array of raw transcript segments (each with 'startTime', 'endTime' and 'text'), you must:\n" +
                    "1. Remove filler words and disfluencies (e.g., 'um', 'uh', 'you know', 'like').\n" +
                    "2. Restore proper punctuation, capitalization, and paragraph breaks.\n" +
                    "3. Normalize numbers, dates, acronyms and special terms consistently.\n" +
                    "4. Preserve original meaning and non-verbal markers.\n" +
                    "5. Do NOT modify timecodes.\n" +
                    "Return ONLY a JSON array of TranscriptSegment objects with the same structure, but with normalizedText filled. Do not include any other text or formatting.";

    @NonNull
    private final ObjectMapper mapper = new ObjectMapper();

    // Maximum number of segments to process in one API call
    private static final int MAX_SEGMENTS_PER_CHUNK = 50;

    //Constructor
    public TranscriptCleanerService(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    /**
     * Processa e normaliza a lista de segmentos.
     * @param segments lista de TranscriptSegment com texto bruto
     * @return lista de TranscriptSegment com normalizedText preenchido
     */
    public List<TranscriptSegment> cleanTranscript(List<TranscriptSegment> segments) throws IOException {
        List<TranscriptSegment> result = new ArrayList<>();
        
        // Split segments into chunks
        for (int i = 0; i < segments.size(); i += MAX_SEGMENTS_PER_CHUNK) {
            int endIndex = Math.min(i + MAX_SEGMENTS_PER_CHUNK, segments.size());
            List<TranscriptSegment> chunk = segments.subList(i, endIndex);
            
            // Process this chunk
            List<TranscriptSegment> cleanedChunk = processChunk(chunk);
            result.addAll(cleanedChunk);
            
            // Add a small delay between chunks to avoid rate limiting
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Processing interrupted", e);
            }
        }
        
        return result;
    }

    /**
     * Process a single chunk of transcript segments.
     * @param chunk List of segments to process
     * @return List of cleaned segments
     */
    private List<TranscriptSegment> processChunk(List<TranscriptSegment> chunk) throws IOException {
        // Serialize the chunk to JSON
        String segmentsJson = mapper.writeValueAsString(chunk);

        // Build the prompt
        StringBuilder prompt = new StringBuilder();
        prompt.append(SYSTEM_PROMPT).append("\n\n");
        prompt.append("Raw transcript segments (JSON array):\n");
        prompt.append(segmentsJson).append("\n\n");
        prompt.append("Return ONLY the cleaned array in the same JSON format, with 'normalizedText' filled for each segment. Do not include any other text or formatting.");

        // Get the cleaned transcript from the AI
        String aiResponse = openAIService.getChatCompletion(prompt.toString()).trim();

        // Extract the JSON array from the response
        String jsonArray = extractJsonArray(aiResponse);
        
        try {
            // Parse the AI's response back into a list of TranscriptSegment objects
            List<TranscriptSegment> cleanedSegments = mapper.readValue(
                jsonArray,
                mapper.getTypeFactory().constructCollectionType(List.class, TranscriptSegment.class)
            );
            return cleanedSegments;
        } catch (Exception e) {
            System.err.println("Failed to parse AI response for chunk: " + aiResponse);
            throw new IOException("Failed to parse AI response: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts a JSON array from the AI response, handling various response formats.
     * @param response The raw response from the AI
     * @return The extracted JSON array as a string
     */
    private String extractJsonArray(String response) {
        // Remove any markdown code block markers
        response = response.replaceAll("```json\\s*", "")
                         .replaceAll("```\\s*", "")
                         .trim();

        // Find the first '[' and last ']'
        int start = response.indexOf('[');
        int end = response.lastIndexOf(']');
        
        if (start == -1 || end == -1 || end <= start) {
            throw new IllegalArgumentException("No valid JSON array found in response: " + response);
        }
        
        return response.substring(start, end + 1);
    }
}

