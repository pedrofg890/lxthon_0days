package lxthon.backend.Service;

import lombok.NonNull;
import org.springframework.stereotype.Service;
import lxthon.backend.Domain.TranscriptSegment;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service that cleans and normalizes raw transcript segments by delegating to an LLM.
 * <p>
 * Uses a context‐aware prompt to remove filler words, restore punctuation,
 * normalize terms, and preserve the original timecodes. Supports both
 * single‐chunk and multi‐chunk processing for large transcripts.
 * </p>
 */
@Service
public class TranscriptCleanerService {

    @NonNull
    private final OpenAIService openAIService;

    /**
     * The system prompt instructing the model how to clean each segment.
     * <p>
     * Specifies removal of disfluencies, restoration of punctuation,
     * normalization of numbers/terms, and preservation of timecodes.
     * </p>
     */
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

    /**
     * Constructs a new TranscriptCleanerService.
     *
     * @param openAIService the service used to send prompts to the LLM
     */
    public TranscriptCleanerService(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    /**
     * Cleans and normalizes the given list of transcript segments.
     * <p>
     * If the number of segments exceeds {@link #MAX_SEGMENTS_PER_CHUNK}, it
     * can be adapted to split into chunks and reassemble, but here it sends
     * the entire list at once.
     * </p>
     *
     * @param segments the raw transcript segments to clean
     * @return a list of TranscriptSegment with {@code normalizedText} populated
     * @throws IOException if parsing the LLM response fails
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
     * Processes a chunk of segments by sending JSON‐serialized segments to the LLM
     * and parsing back the cleaned JSON array.
     *
     * @param chunk the sublist of segments to process
     * @return the cleaned sublist of TranscriptSegment objects
     * @throws IOException if JSON parsing of the LLM response fails
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
     * Extracts the first JSON array substring from the LLM response.
     * <p>
     * Strips markdown fences and locates the array boundaries '[' ... ']'.
     * </p>
     *
     * @param response the raw text response from the LLM
     * @return a valid JSON array string
     * @throws IllegalArgumentException if no valid JSON array is found
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

