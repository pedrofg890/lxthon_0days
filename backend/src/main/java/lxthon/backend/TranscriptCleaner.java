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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Deterministic, context-aware transcript cleaner using ChatGPT-4.0 API.
 *
 * Loads a YouTube transcript JSON (array of {start, duration, text}),
 * sends each segment (with context) to ChatGPT-4.0 for normalization,
 * and outputs a cleaned transcript JSON preserving original timecodes.
 */
public class TranscriptCleaner {
    // System prompt for ChatGPT
    private static final String SYSTEM_PROMPT =
            "You are a deterministic, context-aware transcript cleaner. " +
                    "Given an array of raw YouTube transcript segments (each with 'start' and 'duration'), you must:\n" +
                    "1. Remove filler words and disfluencies (e.g., “um,” “uh,” “you know,” “like”).\n" +
                    "2. Restore proper punctuation, capitalization, and paragraph breaks for readability.\n" +
                    "3. Normalize numbers, dates, acronyms, and special terminology consistently (e.g., “5k” → “five thousand”, “AI” → “artificial intelligence”).\n" +
                    "4. Preserve the speaker’s original meaning, emphasis, and any non-verbal markers (e.g., [laugh], [applause]).\n" +
                    "5. Do NOT modify or remove timecodes; keep 'start' and 'duration' unchanged.\n" +
                    "Output a JSON array of segments, each object with:\n" +
                    "  - 'start': original start time in seconds (float),\n" +
                    "  - 'end': start + duration (float),\n" +
                    "  - 'text': the cleaned, normalized transcript text.\n" +
                    "Ensure deterministic output by setting temperature=0.0 and a fixed max_tokens limit.";

    // Use your OpenAI/ChatGPT API key
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

        List<Segment> segments = mapper.readValue(input, new TypeReference<List<Segment>>() {});
        List<Segment> cleaned = cleanTranscript(segments);
        mapper.writerWithDefaultPrettyPrinter().writeValue(output, cleaned);
        System.out.println("Cleaned transcript written to " + output.getPath());
    }

    public static List<Segment> cleanTranscript(List<Segment> segments) throws IOException {
        List<Segment> result = new ArrayList<>();
        for (int i = 0; i < segments.size(); i++) {
            StringBuilder fullPrompt = new StringBuilder();
            fullPrompt.append(SYSTEM_PROMPT).append("\n\n");

            int startContext = Math.max(0, i - CONTEXT_WINDOW);
            for (int j = startContext; j < i; j++) {
                Segment ctx = segments.get(j);
                fullPrompt.append(String.format("[%.2f→%.2f] %s\n",
                        ctx.getStart(), ctx.getStart() + ctx.getDuration(), ctx.getText()));
            }

            Segment seg = segments.get(i);
            fullPrompt.append(String.format("[%.2f→%.2f] Target: %s\nNormalized:",
                    seg.getStart(), seg.getStart() + seg.getDuration(), seg.getText()));

            String normalized = callChatGPT(fullPrompt.toString()).trim();
            result.add(new Segment(seg.getStart(), seg.getDuration(), normalized));
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

            // Use chatgpt-4.0 model
            Map<String, Object> payload = Map.of(
                    "model", "chatgpt40",
                    "prompt", prompt,
                    "temperature", 0.0,
                    "max_tokens", 512
            );
            StringEntity entity = new StringEntity(mapper.writeValueAsString(payload), ContentType.APPLICATION_JSON);
            post.setEntity(entity);

            try (CloseableHttpResponse response = client.execute(post)) {
                String body;
                try {
                    body = EntityUtils.toString(response.getEntity());
                } catch (ParseException e) {
                    throw new IOException("Error reading response entity", e);
                }
                Map<String, Object> json = mapper.readValue(body, new TypeReference<>() {});
                @SuppressWarnings("unchecked")
                List<Map<String, String>> choices = (List<Map<String, String>>) json.get("choices");
                return choices.get(0).get("text");
            }

        } catch (IOException e) {
            throw new IOException("Error calling ChatGPT API", e);
        }
    }

    public static class Segment {
        private double start;
        private double duration;
        private String text;

        public Segment() { }
        public Segment(double start, double duration, String text) {
            this.start = start;
            this.duration = duration;
            this.text = text;
        }
        public double getStart() { return start; }
        public void setStart(double start) { this.start = start; }
        public double getDuration() { return duration; }
        public void setDuration(double duration) { this.duration = duration; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }
}
