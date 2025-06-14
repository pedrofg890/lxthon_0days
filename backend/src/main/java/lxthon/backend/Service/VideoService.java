package lxthon.backend.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.nio.file.Files;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import org.springframework.stereotype.Service;

import lxthon.backend.Domain.TranscriptSegment;

/**
 * Service for interacting with YouTube videos via yt-dlp:
 * <ul>
 *   <li>Download videos in a specified format.</li>
 *   <li>Fetch metadata (JSON dump) for a video.</li>
 *   <li>Extract auto-generated English subtitles and parse them into {@link TranscriptSegment} objects.</li>
 * </ul>
 */
@Service
public class VideoService {
    
    private final String ytDlpPath;

    /**
     * Constructs a VideoService using a default yt-dlp executable path.
     * <p>
     * You can override this path by modifying the constructor or
     * loading from configuration in application.properties.
     * </p>
     */
    public VideoService() {
        // You can configure this in application.properties
        this.ytDlpPath = "yt-dlp"; // If yt-dlp is in PATH
        // Or use absolute path like: "C:\\path\\to\\yt-dlp.exe" for Windows
    }

    /**
     * Downloads the specified YouTube video in the given format.
     *
     * @param url    the YouTube video URL
     * @param format the format code (e.g., "mp4", "best"); if null, defaults to "best"
     * @return the yt-dlp console output, typically containing file location details
     * @throws IOException          if an I/O error occurs during execution
     * @throws InterruptedException if the download process is interrupted
     */
    public String downloadVideo(String url, String format) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add(ytDlpPath);
        command.add(url);
        command.add("-f");
        command.add(format != null ? format : "best");
        
        return executeCommand(command);
    }

    /**
     * Retrieves metadata for the specified YouTube video as a JSON string.
     *
     * @param url the YouTube video URL
     * @return a JSON dump of video metadata (title, duration, formats, etc.)
     * @throws IOException          if an I/O error occurs during execution
     * @throws InterruptedException if the metadata fetch is interrupted
     */
    public String getVideoInfo(String url) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add(ytDlpPath);
        command.add(url);
        command.add("--dump-json");
        
        return executeCommand(command);
    }

    /**
     * Extracts auto-generated English subtitles from the video and converts
     * them into a list of {@link TranscriptSegment}.
     *
     * @param url the YouTube video URL
     * @return a list of TranscriptSegment objects with start/end times and text
     * @throws IOException          if reading or parsing subtitle files fails
     * @throws InterruptedException if the subtitle extraction process is interrupted
     */
    public List<TranscriptSegment> getTranscript(String url) throws IOException, InterruptedException {
        // Ensure URL has proper protocol
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }
        
        // Create a temporary directory
        File tempDir = Files.createTempDirectory("yt-dlp-subtitles").toFile();
        System.out.println("Creating transcript files in directory: " + tempDir.getAbsolutePath());
        
        try {           
            // Try to get English subtitles
            List<String> command = new ArrayList<>();
            command.add(ytDlpPath);
            command.add(url);
            command.add("--skip-download");
            command.add("--write-auto-sub");
            command.add("--sub-lang");
            command.add("en");
            command.add("--sub-format");
            command.add("json3");
            command.add("--output");
            command.add(tempDir.getAbsolutePath() + File.separator + "%(id)s.%(ext)s");
            
            System.out.println("Executing command: " + String.join(" ", command));
            
            // Execute the command
            String output = executeCommand(command);
            System.out.println("yt-dlp output: " + output);
            
            // Find the JSON file
            File[] allFiles = tempDir.listFiles();
            System.out.println("All files in temp directory: " + Arrays.toString(allFiles));
            
            File[] jsonFiles = tempDir.listFiles((dir, name) -> name.endsWith(".json"));
            
            if (jsonFiles == null || jsonFiles.length == 0) {
                // Try to find any subtitle file
                File[] subFiles = tempDir.listFiles((dir, name) -> 
                    name.contains(".en.json") || name.endsWith(".json"));
                
                if (subFiles != null && subFiles.length > 0) {
                    jsonFiles = subFiles;
                } else {
                    System.out.println("No subtitles found for video: " + url);
                    return Collections.emptyList();
                }
            }
            
            File subtitleFile = jsonFiles[0];
            System.out.println("Processing subtitle file: " + subtitleFile.getName() + " (size: " + subtitleFile.length() + " bytes)");
            
            if (subtitleFile.length() == 0) {
                System.out.println("Subtitle file is empty for video: " + url);
                return Collections.emptyList();
            }
            
            // Parse the JSON file
            List<TranscriptSegment> segments = parseJsonSubtitleFile(subtitleFile);
            
            System.out.println("Successfully extracted " + segments.size() + " transcript segments");
            return segments;
            
        } finally {
            // Clean up temp directory
            try {
                File[] files = tempDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        file.delete();
                    }
                }
                tempDir.delete();
            } catch (Exception e) {
                System.out.println("Warning: Could not clean up temp files: " + e.getMessage());
            }
        }
    }

    /**
     * Parses a yt-dlp-generated subtitle JSON file into TranscriptSegment objects.
     *
     * @param jsonFile the subtitle JSON file
     * @return a list of TranscriptSegment with startTime, endTime, and text
     * @throws IOException if file reading or JSON parsing fails
     */
    private List<TranscriptSegment> parseJsonSubtitleFile(File jsonFile) throws IOException {
        List<TranscriptSegment> segments = new ArrayList<>();
        
        try (FileReader reader = new FileReader(jsonFile)) {
            JSONObject jsonSubtitles = new JSONObject(new JSONTokener(reader));
            JSONArray events = jsonSubtitles.getJSONArray("events");
            
            for (int i = 0; i < events.length(); i++) {
                JSONObject event = events.getJSONObject(i);
                
                // Skip events without text segments
                if (!event.has("segs")) {
                    continue;
                }
                
                // Extract start and end times (in milliseconds)
                double startTime = event.has("tStartMs") ? 
                        event.getDouble("tStartMs") / 1000.0 : 0;
                double endTime = event.has("dDurationMs") ? 
                        startTime + (event.getDouble("dDurationMs") / 1000.0) : 0;
                
                // Extract text from segments
                StringBuilder textBuilder = new StringBuilder();
                JSONArray segs = event.getJSONArray("segs");
                
                for (int j = 0; j < segs.length(); j++) {
                    JSONObject seg = segs.getJSONObject(j);
                    if (seg.has("utf8")) {
                        String text = seg.getString("utf8");
                        if (text != null && !text.trim().isEmpty()) {
                            textBuilder.append(text);
                        }
                    }
                }
                
                String finalText = textBuilder.toString().trim();
                if (!finalText.isEmpty()) {
                    TranscriptSegment segment = new TranscriptSegment();
                    segment.setStartTime(startTime);
                    segment.setEndTime(endTime);
                    segment.setText(finalText);
                    segments.add(segment);
                }
            }
        }
        
        return segments;
    }

    /**
     * Executes the given command line, capturing both standard and error output.
     *
     * @param command the list of command and arguments to run
     * @return the combined console output
     * @throws IOException          if an I/O error occurs
     * @throws InterruptedException if the process is interrupted
     */
    private String executeCommand(List<String> command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process = processBuilder.start();
        
        // Read the output
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        
        // Read any errors
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        while ((line = errorReader.readLine()) != null) {
            output.append("Error: ").append(line).append("\n");
        }
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("yt-dlp process failed with exit code: " + exitCode + "\nOutput: " + output);
        }
        
        return output.toString();
    }
} 
