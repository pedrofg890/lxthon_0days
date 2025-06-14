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
        // Extract all raw text with segment markers
        StringBuilder fullText = new StringBuilder();
        for (int i = 0; i < segments.size(); i++) {
            TranscriptSegment segment = segments.get(i);
            fullText.append("[SEG").append(i).append("]")
                    .append(segment.getText())
                    .append("[/SEG").append(i).append("] ");
        }
        
        // Modify prompt for this approach
        String prompt = "You are a transcript cleaner focused on removing verbal disfluencies.\n\n" +
               "CRITICALLY IMPORTANT: Your primary task is to REMOVE ALL filler words including but not limited to:\n" +
               "- 'uh', 'um', 'er', 'ah', 'eh'\n" +
               "- 'like', 'you know', 'I mean', 'kind of', 'sort of'\n" +
               "- Repeated words ('the the', 'I I I')\n" +
               "- False starts and incomplete phrases\n" +
               "- ANY hesitation sound or unnecessary verbal pause\n\n" +
               
               "Example: \"I um actually uh wanted to like you know see if uh we could...\" → \"I actually wanted to see if we could...\"\n\n" +
               
               "Additional tasks (secondary to removing fillers):\n" +
               "1. Fix grammar and punctuation\n" +
               "2. Normalize numbers and acronyms\n" +
               "3. Preserve meaningful content\n\n" +
               
               "CRITICAL: Keep ALL [SEGx] and [/SEGx] markers EXACTLY as they appear - they are required for processing.\n\n" +
               
               "Return ONLY the cleaned transcript with segment markers preserved.\n\n" +
               
               "Transcript to clean:\n" + 
               fullText.toString();
        
        // Get cleaned text
        String cleanedText = openAIService.getChatCompletion(prompt);
        
        // Process the result
        List<TranscriptSegment> result = new ArrayList<>();
        for (int i = 0; i < segments.size(); i++) {
            String marker = "\\[SEG" + i + "\\](.*?)\\[/SEG" + i + "\\]";
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(marker, java.util.regex.Pattern.DOTALL);
            java.util.regex.Matcher matcher = pattern.matcher(cleanedText);
            
            if (matcher.find()) {
                String cleaned = matcher.group(1).trim();
                TranscriptSegment original = segments.get(i);
                
                TranscriptSegment newSegment = new TranscriptSegment();
                newSegment.setStartTime(original.getStartTime());
                newSegment.setEndTime(original.getEndTime());
                newSegment.setText(original.getText());
                newSegment.setNormalizedText(cleaned);
                
                result.add(newSegment);
            } else {
                // Fallback if segment not found - keep original
                TranscriptSegment original = segments.get(i);
                TranscriptSegment newSegment = new TranscriptSegment();
                newSegment.setStartTime(original.getStartTime());
                newSegment.setEndTime(original.getEndTime());
                newSegment.setText(original.getText());
                newSegment.setNormalizedText(original.getText());
                result.add(newSegment);
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

