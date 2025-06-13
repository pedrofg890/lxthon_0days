package lxthon.backend.Domain;

public class TranscriptSegment {
    private double startTime;
    private double endTime;
    private String text;
    private String normalizedText;

    public TranscriptSegment() {}

    public TranscriptSegment(double startTime, double endTime, String text, String normalizedText) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.text = text;
        this.normalizedText = normalizedText;
    }

    public double getStartTime() { return startTime; }
    public void setStartTime(double startTime) { this.startTime = startTime; }

    public double getEndTime() { return endTime; }
    public void setEndTime(double endTime) { this.endTime = endTime; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getNormalizedText() { return normalizedText; }
    public void setNormalizedText(String normalizedText) { this.normalizedText = normalizedText; }
}