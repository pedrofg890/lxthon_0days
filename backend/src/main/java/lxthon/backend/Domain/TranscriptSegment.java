package lxthon.backend.Domain;

/**
 * Represents a segment of a transcript with its timecodes and associated texts.
 * <p>
 * Each segment contains the original text, its normalized form, and the start/end times
 * in seconds indicating where this segment appears in the video.
 * </p>
 */
public class TranscriptSegment {
    /**
     * The start time of this segment in seconds.
     * <p>
     * Indicates when this segment begins in the original video or audio.
     * </p>
     */
    private double startTime;

    /**
     * The end time of this segment in seconds.
     * <p>
     * Calculated as startTime + duration or provided directly, indicating when this
     * segment finishes in the original video or audio.
     * </p>
     */
    private double endTime;

    /**
     * The original transcript text for this segment.
     * <p>
     * This is the raw text extracted from subtitles or speech-to-text, including
     * any filler words or disfluencies.
     * </p>
     */
    private String text;

    /**
     * The cleaned or normalized transcript text for this segment.
     * <p>
     * This text has had filler words removed, punctuation restored, and casing
     * normalized for readability.
     * </p>
     */
    private String normalizedText;

    /**
     * Default constructor for frameworks that require a no-arg constructor.
     */
    public TranscriptSegment() {}


    /**
     * Constructs a TranscriptSegment with all fields specified.
     *
     * @param startTime      the start time of the segment in seconds
     * @param endTime        the end time of the segment in seconds
     * @param text           the original transcript text
     * @param normalizedText the cleaned and normalized transcript text
     */
    public TranscriptSegment (double startTime, double endTime, String text, String normalizedText) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.text = text;
        this.normalizedText = normalizedText;
    }

    /**
     * Returns the start time of this segment in seconds.
     *
     * @return the start time in seconds
     */
    public double getStartTime() { return startTime; }

    /**
     * Sets the start time of this segment.
     *
     * @param startTime the start time in seconds
     */
    public void setStartTime(double startTime) { this.startTime = startTime; }

    /**
     * Returns the end time of this segment in seconds.
     *
     * @return the end time in seconds
     */
    public double getEndTime() { return endTime; }

    /**
     * Sets the end time of this segment.
     *
     * @param endTime the end time in seconds
     */
    public void setEndTime(double endTime) { this.endTime = endTime; }

    /**
     * Returns the original transcript text of this segment.
     *
     * @return the original transcript text
     */
    public String getText() { return text; }

    /**
     * Sets the original transcript text of this segment.
     *
     * @param text the raw transcript text
     */
    public void setText(String text) { this.text = text; }

    /**
     * Returns the normalized transcript text of this segment.
     *
     * @return the cleaned and normalized text
     */
    public String getNormalizedText() { return normalizedText; }

    /**
     * Sets the normalized transcript text of this segment.
     *
     * @param normalizedText the cleaned and normalized transcript text
     */
    public void setNormalizedText(String normalizedText) { this.normalizedText = normalizedText; }
}