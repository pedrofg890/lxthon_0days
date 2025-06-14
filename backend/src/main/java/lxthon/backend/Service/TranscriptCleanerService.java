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

    // Maximum number of characters per chunk
    private static final int CHARS_PER_CHUNK = 4000;

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
        // Step 1: Extract all text without markers
        StringBuilder fullText = new StringBuilder();
        for (TranscriptSegment segment : segments) {
            fullText.append(segment.getText()).append(" ");
        }
        String allText = fullText.toString();
        
        // Step 2: Split text into chunks by character count
        List<String> textChunks = new ArrayList<>();
        for (int i = 0; i < allText.length(); i += CHARS_PER_CHUNK) {
            int end = Math.min(i + CHARS_PER_CHUNK, allText.length());
            
            // Adjust end to avoid breaking in the middle of a word
            if (end < allText.length()) {
                int spacePos = allText.lastIndexOf(" ", end);
                if (spacePos > i) {
                    end = spacePos + 1;
                }
            }
            
            textChunks.add(allText.substring(i, end));
        }
        
        // Step 3: Clean each text chunk
        StringBuilder cleanedFullText = new StringBuilder();
        for (String chunk : textChunks) {
            String cleanedChunk = cleanTextChunk(chunk);
            cleanedFullText.append(cleanedChunk).append(" ");
            
            // Add delay between API calls
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Step 4: Split cleaned text back into segments based on word proportion
        String cleanedText = cleanedFullText.toString().trim();
        String[] originalWords = allText.split("\\s+");
        String[] cleanedWords = cleanedText.split("\\s+");
        
        // Calculate ratio of cleaned words to original words
        double ratio = (double) cleanedWords.length / originalWords.length;
        
        List<TranscriptSegment> result = new ArrayList<>();
        int currentWordIndex = 0;
        
        for (TranscriptSegment originalSegment : segments) {
            int originalWordCount = originalSegment.getText().split("\\s+").length;
            int estimatedCleanedWordCount = (int) Math.round(originalWordCount * ratio);
            estimatedCleanedWordCount = Math.max(1, Math.min(estimatedCleanedWordCount, 
                                             cleanedWords.length - currentWordIndex));
            
            // Create cleaned segment text
            StringBuilder segmentText = new StringBuilder();
            for (int i = 0; i < estimatedCleanedWordCount && currentWordIndex < cleanedWords.length; i++) {
                if (i > 0) segmentText.append(" ");
                segmentText.append(cleanedWords[currentWordIndex++]);
            }
            
            // Create new segment with cleaned text
            TranscriptSegment newSegment = new TranscriptSegment();
            newSegment.setStartTime(originalSegment.getStartTime());
            newSegment.setEndTime(originalSegment.getEndTime());
            newSegment.setText(originalSegment.getText());
            newSegment.setNormalizedText(segmentText.toString());
            result.add(newSegment);
        }
        
        return result;
    }

    private String cleanTextChunk(String chunk) throws IOException {
        String prompt = "You are a transcript cleaner focused on removing verbal disfluencies.\n\n" +
                       "CRITICALLY IMPORTANT:\n" +
                       "1. Remove ALL filler words including:\n" +
                       "   - 'uh', 'um', 'er', 'ah', 'eh'\n" +
                       "   - 'like', 'you know', 'I mean'\n" +
                       "   - Repeated words and false starts\n\n" +
                       
                       "2. DO NOT include ANY markers, tags, or special formatting in your response.\n" +
                       "   - Remove any [SEGx] or [/SEGx] markers if present\n" +
                       "   - Return ONLY the cleaned text\n\n" +
                       
                       "Example: \"I um actually uh wanted to like you know see if uh we could...\" → \"I actually wanted to see if we could...\"\n\n" +
                       
                       "Also fix grammar/punctuation and normalize formatting.\n\n" +
                       
                       "Text to clean:\n" + chunk;
        
        String cleanedText = openAIService.getChatCompletion(prompt);
        
        // Post-process to remove any remaining markers
        return cleanedText.replaceAll("\\[SEG\\d+\\]|\\[/SEG\\d+\\]", "").trim();
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

