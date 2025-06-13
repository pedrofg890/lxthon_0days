package lxthon.backend.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.nio.file.Files;

import org.springframework.stereotype.Service;

import lxthon.backend.Domain.TranscriptSegment;

@Service
public class YoutubeService {
    
    private final String ytDlpPath;
    
    public YoutubeService() {
        // You can configure this in application.properties
        this.ytDlpPath = "yt-dlp"; // If yt-dlp is in PATH
        // Or use absolute path like: "C:\\path\\to\\yt-dlp.exe" for Windows
    }
    
    public String downloadVideo(String url, String format) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add(ytDlpPath);
        command.add(url);
        command.add("-f");
        command.add(format != null ? format : "best");
        
        return executeCommand(command);
    }
    
    public String getVideoInfo(String url) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add(ytDlpPath);
        command.add(url);
        command.add("--dump-json");
        
        return executeCommand(command);
    }
    
    public List<TranscriptSegment> getTranscript(String url) throws IOException, InterruptedException {
        // Ensure URL has proper protocol
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }
        
        // Create a temporary directory
        File tempDir = Files.createTempDirectory("yt-dlp-subtitles").toFile();
        System.out.println("Creating transcript files in directory: " + tempDir.getAbsolutePath());
        
        List<String> command = new ArrayList<>();
        command.add(ytDlpPath);
        command.add(url);
        command.add("--skip-download");
        command.add("--write-auto-sub");
        command.add("--sub-lang");
        command.add("en");
        command.add("--sub-format");
        command.add("vtt");
        command.add("--output");
        command.add(tempDir.getAbsolutePath() + File.separator + "subtitle");
        
        // Execute the command
        String output = executeCommand(command);
        System.out.println("yt-dlp executed successfully");
        
        // Find the VTT file
        File[] vttFiles = tempDir.listFiles((dir, name) -> name.endsWith(".vtt"));
        
        if (vttFiles == null || vttFiles.length == 0) {
            File[] allFiles = tempDir.listFiles();
            throw new IOException("No VTT files found. Files: " + Arrays.toString(allFiles));
        }
        
        File subtitleFile = vttFiles[0];
        System.out.println("Processing file: " + subtitleFile.getName());
        
        // Parse the VTT file
        List<TranscriptSegment> segments = parseVttFile(subtitleFile);
        
        // Clean up
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
        
        System.out.println("Extracted " + segments.size() + " transcript segments");
        return segments;
    }
    
    private List<TranscriptSegment> parseVttFile(File vttFile) throws IOException {
        List<TranscriptSegment> segments = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(vttFile))) {
            String line;
            boolean skipHeader = true;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                // Skip WEBVTT header and empty lines at start
                if (skipHeader) {
                    if (line.isEmpty() || line.startsWith("WEBVTT") || line.startsWith("NOTE")) {
                        continue;
                    }
                    skipHeader = false;
                }
                
                // Skip empty lines and sequence numbers
                if (line.isEmpty() || line.matches("^\\d+$")) {
                    continue;
                }
                
                // Process timestamp lines
                if (line.contains("-->")) {
                    String[] times = line.split("-->");
                    if (times.length == 2) {
                        try {
                            double startTime = parseVttTime(times[0].trim());
                            double endTime = parseVttTime(times[1].trim());
                            
                            // Read the text lines for this segment
                            StringBuilder textBuilder = new StringBuilder();
                            String textLine;
                            while ((textLine = reader.readLine()) != null) {
                                textLine = textLine.trim();
                                if (textLine.isEmpty()) break; // End of this segment
                                
                                // Clean VTT formatting
                                String cleanedText = cleanVttFormatting(textLine);
                                if (!cleanedText.isEmpty()) {
                                    if (textBuilder.length() > 0) {
                                        textBuilder.append(" ");
                                    }
                                    textBuilder.append(cleanedText);
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
                            
                        } catch (Exception e) {
                            System.out.println("Error parsing timestamp: " + line + " - " + e.getMessage());
                        }
                    }
                }
            }
        }
        
        return segments;
    }
    
    private double parseVttTime(String timeStr) {
        // Remove any positioning info after the timestamp
        timeStr = timeStr.split("\\s+")[0];
        
        // Parse HH:MM:SS.mmm format
        String[] parts = timeStr.split("[:\\.]");
        if (parts.length == 4) {
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            int seconds = Integer.parseInt(parts[2]);
            int milliseconds = Integer.parseInt(parts[3]);
            
            return hours * 3600 + minutes * 60 + seconds + milliseconds / 1000.0;
        }
        throw new IllegalArgumentException("Invalid time format: " + timeStr);
    }
    
    private String cleanVttFormatting(String text) {
        if (text == null) return "";
        
        // Remove VTT timing tags like <00:00:03.199>
        text = text.replaceAll("<\\d{2}:\\d{2}:\\d{2}\\.\\d{3}>", "");
        // Remove color/style tags like <c>, </c>, <c.colorname>
        text = text.replaceAll("</?c[^>]*>", "");
        // Remove any other HTML-like tags
        text = text.replaceAll("<[^>]+>", "");
        // Clean up multiple spaces
        text = text.replaceAll("\\s+", " ").trim();
        
        return text;
    }
    
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
