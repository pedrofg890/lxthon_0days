package java;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Deterministic, context-aware transcript cleaner in Java.
 */
public class TranscriptCleaner {
    private static final double TEMPERATURE = 0.0;
    private static final String MODEL = "gpt-4o-mini"; // Adjust as needed
    private static final String SYSTEM_PROMPT =
            "You are a deterministic, context-aware transcript cleaner. " +
                    "Given raw transcript segments with timecodes, you must:\n" +
                    "- Remove filler words and disfluencies like 'um', 'uh', 'you know'.\n" +
                    "- Restore proper punctuation and casing.\n" +
                    "- Normalize numbers, dates, and special terms consistently.\n" +
                    "- Preserve the original meaning and context.\n" +
                    "- Do NOT alter the timecodes.\n" +
                    "Return a JSON array of segments, each with 'start', 'end', and 'text'.";

    private final OpenAiService service;
    private final ObjectMapper mapper;

    public TranscriptCleaner(String apiKey) {
        service = new OpenAiService(apiKey, Duration.ofSeconds(30));
        mapper = new ObjectMapper();
    }

    static class Segment {
        public double start;
        public double duration;
        public String text;
        public double end;
    }

    private List<Segment> loadTranscript(String path) throws IOException {
        List<Segment> segments = mapper.readValue(
                new File(path), new TypeReference<List<Segment>>() {}
        );
        for (Segment seg : segments) {
            seg.end = seg.start + seg.duration;
        }
        return segments;
    }

    private List<List<Segment>> chunkSegments(List<Segment> segments, int maxChars) {
        List<List<Segment>> chunks = new ArrayList<>();
        List<Segment> current = new ArrayList<>();
        int total = 0;
        for (Segment seg : segments) {
            int len = seg.text.length();
            if (!current.isEmpty() && total + len > maxChars) {
                chunks.add(new ArrayList<>(current));
                current.clear(); total = 0;
            }
            current.add(seg);
            total += len;
        }
        if (!current.isEmpty()) chunks.add(current);
        return chunks;
    }

    private String buildPrompt(List<Segment> chunk) {
        StringBuilder sb = new StringBuilder();
        sb.append("Here are transcript segments with timecodes (seconds):\n");
        for (Segment seg : chunk) {
            sb.append(String.format("[%.2f-%.2f]: %s\n", seg.start, seg.end, seg.text));
        }
        sb.append("\nClean these segments as instructed, preserving the timecodes in your JSON output.");
        return sb.toString();
    }

    private String callLLM(String prompt) {
        List<ChatMessage> messages = List.of(
                new ChatMessage("system", SYSTEM_PROMPT),
                new ChatMessage("user", prompt)
        );
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(MODEL)
                .temperature(TEMPERATURE)
                .messages(messages)
                .build();
        ChatCompletionResult result = service.createChatCompletion(request);
        return result.getChoices().get(0).getMessage().getContent();
    }

    private List<ObjectNode> parseResponse(String raw) throws IOException {
        return mapper.readValue(raw, new TypeReference<List<ObjectNode>>() {});
    }

    public void clean(String inputPath, String outputPath) throws IOException, InterruptedException {
        List<Segment> segments = loadTranscript(inputPath);
        List<List<Segment>> chunks = chunkSegments(segments, 1500);
        List<ObjectNode> cleanedAll = new ArrayList<>();

        for (List<Segment> chunk : chunks) {
            String prompt = buildPrompt(chunk);
            String raw = callLLM(prompt);
            List<ObjectNode> cleaned = parseResponse(raw);
            cleanedAll.addAll(cleaned);
            Thread.sleep(1000); // rate limit buffer
        }

        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(Paths.get(outputPath).toFile(), cleanedAll);
        System.out.println("Cleaned transcript written to " + outputPath);
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: java TranscriptCleaner <input.json> <output.json>");
            System.exit(1);
        }
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("Please set the OPENAI_API_KEY environment variable.");
            System.exit(1);
        }
        TranscriptCleaner cleaner = new TranscriptCleaner(apiKey);
        cleaner.clean(args[0], args[1]);
    }
}