package lxthon.backend.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

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
        // Create a temporary file with .srt extension
        File tempFile = File.createTempFile("transcript", ".srt");
        String tempFilePath = tempFile.getAbsolutePath();
        
        List<String> command = new ArrayList<>();
        command.add(ytDlpPath);
        command.add(url);
        command.add("--write-auto-sub");
        command.add("--sub-lang");
        command.add("en");
        command.add("--write-sub");
        command.add("--sub-format");
        command.add("srt");
        command.add("--output");
        command.add(tempFilePath);
        
        // Execute the command to download the transcript
        String output = executeCommand(command);
        
        // Check if the file was created
        if (!tempFile.exists()) {
            throw new IOException("Failed to create transcript file. yt-dlp output: " + output);
        }
        
        // Read and parse the SRT file
        List<TranscriptSegment> segments = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(tempFile))) {
            String line;
            TranscriptSegment currentSegment = null;
            StringBuilder textBuilder = new StringBuilder();
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                // Skip empty lines
                if (line.isEmpty()) {
                    if (currentSegment != null) {
                        currentSegment.setText(textBuilder.toString().trim());
                        segments.add(currentSegment);
                        currentSegment = null;
                        textBuilder = new StringBuilder();
                    }
                    continue;
                }
                
                // Parse segment number
                if (currentSegment == null) {
                    currentSegment = new TranscriptSegment();
                    continue;
                }
                
                // Parse timestamp
                if (line.contains("-->")) {
                    String[] times = line.split("-->");
                    String startTimeStr = times[0].trim();
                    String endTimeStr = times[1].trim();
                    
                    // Convert SRT time format (HH:MM:SS,mmm) to seconds
                    currentSegment.setStartTime(convertSrtTimeToSeconds(startTimeStr));
                    currentSegment.setEndTime(convertSrtTimeToSeconds(endTimeStr));
                    continue;
                }
                
                // Add text to current segment
                if (currentSegment != null) {
                    textBuilder.append(line).append(" ");
                }
            }
            
            // Add the last segment if exists
            if (currentSegment != null) {
                currentSegment.setText(textBuilder.toString().trim());
                segments.add(currentSegment);
            }
        } finally {
            // Clean up the temporary file
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
        
        return segments;
    }
    
    private double convertSrtTimeToSeconds(String srtTime) {
        // Format: HH:MM:SS,mmm
        String[] parts = srtTime.split("[:,]");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);
        int milliseconds = Integer.parseInt(parts[3]);
        
        return hours * 3600 + minutes * 60 + seconds + milliseconds / 1000.0;
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
