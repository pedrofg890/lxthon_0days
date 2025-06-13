package lxthon.backend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.Timeout;
import lxthon.backend.Domain.TranscriptSegment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TranscriptCleaner utilizando ChatGPT-4.0 API e TranscriptSegment.
 *
 * Lê um JSON de segmentos brutos ({start, duration, text}),
 * aplica limpeza/contextualização via ChatGPT-4.0,
 * e produz JSON com campos {startTime, endTime, text, normalizedText}.
 */
public class TranscriptCleaner {
    private static final String SYSTEM_PROMPT =
            "You are a deterministic, context-aware transcript cleaner. " +
                    "Given an array of raw YouTube transcript segments (each with 'start' and 'duration'), you must:\n" +
                    "1. Remove filler words and disfluencies (e.g., 'um,' 'uh,' 'you know,' 'like').\n" +
                    "2. Restore proper punctuation, capitalization, and paragraph breaks for readability.\n" +
                    "3. Normalize numbers, dates, acronyms, and special terminology consistently (e.g., '5k' → 'five thousand', 'AI' → 'artificial intelligence').\n" +
                    "4. Preserve the speaker’s original meaning, emphasis, and any non-verbal markers (e.g., [laugh], [applause]).\n" +
                    "5. Do NOT modify or remove timecodes; keep 'startTime' and 'endTime' unchanged.\n" +
                    "Output a JSON array of TranscriptSegment objects, each with startTime, endTime, text, normalizedText.\n" +
                    "Ensure deterministic output with temperature=0.0 and fixed max_tokens.";

    private static final String API_KEY = System.getenv("OPENAI_API_KEY");
    private static final String ENDPOINT = System.getenv("CHATGPT_ENDPOINT");
    private static final int CONTEXT_WINDOW = 3;
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage: java TranscriptCleaner <input.json> <output.json>");
            System.exit(1);
        }
        File input = new File(args[0]);
        File output = new File(args[1]);

        // Carrega segmentos brutos do JSON de entrada
        List<Map<String, Object>> rawSegments = mapper.readValue(
                input, new TypeReference<List<Map<String, Object>>>(){});
        List<TranscriptSegment> segments = new ArrayList<>();
        for (Map<String, Object> raw : rawSegments) {
            double start = ((Number) raw.get("start")).doubleValue();
            double duration = ((Number) raw.get("duration")).doubleValue();
            String text = (String) raw.get("text");
            double end = start + duration;
            segments.add(new TranscriptSegment(start, end, text, null));
        }

        // Executa limpeza/contextualização
        List<TranscriptSegment> cleaned = cleanTranscript(segments);

        // Grava resultado em JSON
        mapper.writerWithDefaultPrettyPrinter().writeValue(output, cleaned);
        System.out.println("Cleaned transcript written to " + output.getPath());
    }

    public static List<TranscriptSegment> cleanTranscript(List<TranscriptSegment> segments) throws IOException {
        List<TranscriptSegment> result = new ArrayList<>();
        for (int i = 0; i < segments.size(); i++) {
            TranscriptSegment seg = segments.get(i);

            // Constrói prompt com contexto
            StringBuilder prompt = new StringBuilder();
            prompt.append(SYSTEM_PROMPT).append("\n\n");
            int startCtx = Math.max(0, i - CONTEXT_WINDOW);
            for (int j = startCtx; j < i; j++) {
                TranscriptSegment ctx = segments.get(j);
                prompt.append(String.format("[%.2f→%.2f] %s\n",
                        ctx.getStartTime(), ctx.getEndTime(), ctx.getText()));
            }
            prompt.append(String.format("[%.2f→%.2f] Target: %s\nNormalized:",
                    seg.getStartTime(), seg.getEndTime(), seg.getText()));

            String norm = callChatGPT(prompt.toString()).trim();
            // Preenche normalizedText
            result.add(new TranscriptSegment(
                    seg.getStartTime(), seg.getEndTime(), seg.getText(), norm
            ));
        }
        return result;
    }

    private static String callChatGPT(String prompt) throws IOException {
        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setResponseTimeout(Timeout.ofSeconds(30))
                        .build())
                .build()) {

            HttpPost post = new HttpPost(ENDPOINT);
            post.addHeader("Authorization", "Bearer " + API_KEY);
            post.addHeader("Content-Type", "application/json");

            Map<String, Object> payload = Map.of(
                    "model", "chatgpt40",
                    "prompt", prompt,
                    "temperature", 0.0,
                    "max_tokens", 512
            );
            post.setEntity(new StringEntity(mapper.writeValueAsString(payload), ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse resp = client.execute(post)) {
                String body;
                try {
                    body = EntityUtils.toString(resp.getEntity());
                } catch (ParseException e) {
                    throw new IOException("Error reading response entity", e);
                }
                Map<String, Object> json = mapper.readValue(body, new TypeReference<>(){});
                @SuppressWarnings("unchecked")
                List<Map<String, String>> choices =
                        (List<Map<String, String>>) json.get("choices");
                return choices.get(0).get("text");
            }
        } catch (IOException e) {
            throw new IOException("Error calling ChatGPT API", e);
        }
    }
}
