package lxthon.backend.Service;

import lombok.NonNull;
import org.springframework.stereotype.Service;


@Service
public class PodcastService {

    @NonNull
    private final VideoService youtubeService;

    @NonNull
    private final OpenAIService openAIService;

    //@NonNull
    //private final ElevenLabsService elevenLabsService;

    public PodcastService(@NonNull VideoService youtubeService, @NonNull OpenAIService openAIService) {
        this.youtubeService = youtubeService;
        this.openAIService = openAIService;
    }



//    public byte[] createPodcastFromYoutube(String url) throws Exception {
//        // 1. Get transcript
//        List<TranscriptSegment> transcript = youtubeService.getTranscript(url);
//
//        // 2. Summarize or chunk transcript
//        String summary = openAIService.summarizeTranscript(transcript);
//
//        // 3. Generate dialogue
//        List<DialogueTurn> dialogue = openAIService.generateDialogue(summary);
//
//        // 4. Synthesize each turn
//        List<byte[]> audioClips = new ArrayList<>();
//        for (DialogueTurn turn : dialogue) {
//            byte[] audio = elevenLabsService.synthesize(turn.getText(), turn.getSpeaker());
//            audioClips.add(audio);
//        }
//
//        // 5. Concatenate audio
//        byte[] podcast = audioUtils.concatenate(audioClips);
//
//        // 6. Return
//        return podcast;
//    }
}