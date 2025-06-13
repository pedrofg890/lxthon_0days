package lxthon.backend.Service.PodcastGeneration;

import lombok.NonNull;
import lxthon.backend.Service.OpenAIService;
import lxthon.backend.Service.TranscriptCleanerService;
import lxthon.backend.Service.VideoService;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lxthon.backend.Domain.TranscriptSegment;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class PodcastService {

    private static final Logger log = LoggerFactory.getLogger(PodcastService.class);

    @NonNull
    private final VideoService videoService;

    @NonNull
    private final OpenAIService openAIService;

    @NonNull
    private final VideoToSpeechService videoToSpeechService;

    @NonNull
    private final TranscriptCleanerService transcriptCleanerService;

    public PodcastService(@NonNull VideoService videoService,
                          @NonNull OpenAIService openAIService,
                          @NonNull VideoToSpeechService videoToSpeechService,
                          @NonNull TranscriptCleanerService transcriptCleanerService) {
        this.videoService = videoService;
        this.openAIService = openAIService;
        this.videoToSpeechService = videoToSpeechService;
        this.transcriptCleanerService = transcriptCleanerService;
    }

    /**
     * Generate a complete podcast from a YouTube video URL
     * @param videoUrl YouTube video URL
     * @param hostAName Name of first host
     * @param hostBName Name of second host
     * @return PodcastResult containing script and audio
     */
    public PodcastResult generatePodcastFromVideo(String videoUrl, String hostAName, String hostBName)
            throws IOException, InterruptedException {

        log.info("Starting podcast generation for video: {}", videoUrl);

        // Step 1: Extract and clean transcript
        log.info("Step 1: Extracting transcript...");
        List<TranscriptSegment> rawTranscript = videoService.getTranscript(videoUrl);

        log.info("Step 2: Cleaning transcript...");
        List<TranscriptSegment> cleanedTranscript = transcriptCleanerService.cleanTranscript(rawTranscript);

        // Step 3: Generate podcast conversation script
        log.info("Step 3: Generating podcast script...");
        String podcastScript = generatePodcastScript(cleanedTranscript, hostAName, hostBName);

        // Step 4: Generate audio for the podcast
        log.info("Step 4: Generating podcast audio...");
        byte[] podcastAudio = generatePodcastAudio(podcastScript);

        log.info("Podcast generation completed successfully");

        return new PodcastResult(podcastScript, podcastAudio, hostAName, hostBName);
    }

    /**
     * Generate podcast script using OpenAI
     */
    private String generatePodcastScript(List<TranscriptSegment> cleanedTranscript,
                                         String hostAName, String hostBName) {

        // Combine all cleaned text
        String fullTranscript = cleanedTranscript.stream()
                .map(segment -> segment.getNormalizedText() != null ?
                        segment.getNormalizedText() : segment.getText())
                .collect(Collectors.joining(" "));

        // Create the prompt for OpenAI
        String prompt = String.format("""
            Transform the following content into an engaging podcast conversation between two hosts.
            
            HOSTS:
            - %s: An enthusiastic tech educator who explains concepts clearly and engagingly
            - %s: A curious interviewer who asks insightful questions and provides thoughtful reactions
            
            REQUIREMENTS:
            - Create natural, conversational dialogue
            - Break down complex concepts into simple explanations
            - Include natural reactions, transitions, and speech patterns
            - Make it engaging for a general audience
            - Target 5-8 minutes of conversation
            - Include occasional humor and interesting analogies
            - End with key takeaways
            
            FORMAT (very important - use exactly this format):
            %s: [Speaker A's dialogue]
            %s: [Speaker B's dialogue]
            %s: [Speaker A's response]
            %s: [Speaker B's follow-up]
            
            CONTENT TO TRANSFORM:
            %s
            
            Generate an engaging podcast conversation:
            """,
                hostAName, hostBName,
                hostAName, hostBName, hostAName, hostBName,
                fullTranscript);

        return openAIService.getChatCompletion(prompt);
    }

    /**
     * Generate audio for the entire podcast script
     */
    private byte[] generatePodcastAudio(String script) throws IOException, InterruptedException {
        log.info("Converting podcast script to audio...");

        ByteArrayOutputStream audioStream = new ByteArrayOutputStream();

        // Parse the script and generate audio for each speaker
        String[] lines = script.split("\n");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // Check if line starts with a host name followed by colon
            if (line.contains(":")) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    String speaker = parts[0].trim();
                    String dialogue = parts[1].trim();

                    if (!dialogue.isEmpty()) {
                        byte[] audioSegment;

                        // Determine which voice to use based on speaker position
                        // (First mentioned speaker = Host A, Second = Host B)
                        if (isFirstSpeaker(speaker, script)) {
                            audioSegment = videoToSpeechService.generateHostASpeech(dialogue);
                            log.debug("Generated audio for Host A: {}", dialogue.substring(0, Math.min(50, dialogue.length())));
                        } else {
                            audioSegment = videoToSpeechService.generateHostBSpeech(dialogue);
                            log.debug("Generated audio for Host B: {}", dialogue.substring(0, Math.min(50, dialogue.length())));
                        }

                        audioStream.write(audioSegment);

                        // Add a small pause between speakers (optional)
                        addPauseBetweenSpeakers(audioStream);
                    }
                }
            }
        }

        return audioStream.toByteArray();
    }

    /**
     * Determine if this is the first speaker mentioned in the script
     */
    private boolean isFirstSpeaker(String speaker, String script) {
        // Find all speaker names in the script
        Pattern pattern = Pattern.compile("^([^:]+):", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(script);

        if (matcher.find()) {
            String firstSpeaker = matcher.group(1).trim();
            return speaker.equals(firstSpeaker);
        }

        return true; // Default to first speaker
    }

    /**
     * Add a small pause between speakers (mock implementation)
     */
    private void addPauseBetweenSpeakers(ByteArrayOutputStream audioStream) {
        // Add a small silence (mock - in reality you'd add actual silence audio)
        try {
            Thread.sleep(100); // Small delay to simulate pause
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Data class to hold podcast generation results
     */
    public static class PodcastResult {
        private final String script;
        private final byte[] audio;
        private final String hostAName;
        private final String hostBName;

        public PodcastResult(String script, byte[] audio, String hostAName, String hostBName) {
            this.script = script;
            this.audio = audio;
            this.hostAName = hostAName;
            this.hostBName = hostBName;
        }

        // Getters
        public String getScript() { return script; }
        public byte[] getAudio() { return audio; }
        public String getHostAName() { return hostAName; }
        public String getHostBName() { return hostBName; }
        public int getAudioSizeBytes() { return audio != null ? audio.length : 0; }
    }
}