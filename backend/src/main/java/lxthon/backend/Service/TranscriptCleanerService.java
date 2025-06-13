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
                    "Return a JSON array of TranscriptSegment objects.";

    @NonNull
    private static final int CONTEXT_WINDOW = 3;

    @NonNull
    private final ObjectMapper mapper = new ObjectMapper();

    //Constructor
    public TranscriptCleanerService(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    /**
     * Processa e normaliza a lista de segmentos.
     * @param segments lista de TranscriptSegment com texto bruto
     * @return lista de TranscriptSegment com normalizedText preenchido
     */
    public List<TranscriptSegment> cleanTranscript (List<TranscriptSegment> segments) throws IOException {
        List<TranscriptSegment> result = new ArrayList<>();
        for (int i = 0; i < segments.size(); i++) {
            TranscriptSegment seg = segments.get(i);
            StringBuilder prompt = new StringBuilder();
            prompt.append(SYSTEM_PROMPT).append("\n\n");

            int ctxStart = Math.max(0, i - CONTEXT_WINDOW);
            for (int j = ctxStart; j < i; j++) {
                TranscriptSegment ctx = segments.get(j);
                prompt.append(String.format("[%.2f→%.2f] %s\n",
                        ctx.getStartTime(), ctx.getEndTime(), ctx.getText()));
            }
            prompt.append(String.format("[%.2f→%.2f] Target: %s\nNormalized:",
                    seg.getStartTime(), seg.getEndTime(), seg.getText()));

            String normalized = openAIService.getChatCompletion(prompt.toString()).trim();
            result.add(new TranscriptSegment(
                    seg.getStartTime(), seg.getEndTime(), seg.getText(), normalized
            ));
        }
        return result;
    }
}

