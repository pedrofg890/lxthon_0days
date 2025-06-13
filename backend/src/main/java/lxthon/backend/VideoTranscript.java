package lxthon.backend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class VideoTranscript {
    @Getter
    private final String videoId;
    @Getter
    private final String title;
    @Getter
    private final List<TranscriptCleaner.Segment> segments;

    public static VideoTranscript loadFromJson (File file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        // Espera JSON com campos videoId, title e array de segmentos
        Map<String, Object> map = mapper.readValue(file, new TypeReference<>() {});
        String vid = (String) map.get("videoId");
        String ttl = (String) map.get("title");
        List<TranscriptCleaner.Segment> segs = mapper.convertValue(
                map.get("segments"),
                new TypeReference<List<TranscriptCleaner.Segment>>(){}
        );
        return new VideoTranscript(vid, ttl, segs);
    }


}
