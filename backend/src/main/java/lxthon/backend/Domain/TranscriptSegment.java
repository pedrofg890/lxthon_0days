package lxthon.backend.Domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TranscriptSegment {
    private double startTime;
    private double endTime;
    private String text;
    private String normalizedText; 
}