package lxthon.backend.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
        
        // Create a temporary directory instead of a specific file
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
        command.add(tempDir.getAbsolutePath() + File.separator + "%(title)s.%(ext)s");
        command.add("--verbose");
        
        // Debug: Print the exact command being executed
        System.out.println("Executing command: " + String.join(" ", command));
        
        // Execute the command to download the transcript
        String output = executeCommand(command);
        System.out.println("yt-dlp output: " + output);
        
        // Find the created .vtt file in the temp directory
        File[] vttFiles = tempDir.listFiles((dir, name) -> name.endsWith(".vtt"));
        
        if (vttFiles == null || vttFiles.length == 0) {
            throw new IOException("No VTT subtitle files were created. yt-dlp output: " + output);
        }
        
        File subtitleFile = vttFiles[0]; // Take the first .vtt file found
        System.out.println("Found subtitle file: " + subtitleFile.getAbsolutePath());
        
        if (subtitleFile.length() == 0) {
            throw new IOException("Subtitle file is empty. yt-dlp output: " + output);
        }
        
        // Now read the file and parse it
        List<TranscriptSegment> segments = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(subtitleFile))) {
            String line;
            TranscriptSegment currentSegment = null;
            StringBuilder textBuilder = new StringBuilder();
            boolean isHeader = true;
            boolean isInSegment = false;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                // Skip header lines (WEBVTT and empty lines at start)
                if (isHeader) {
                    if (line.isEmpty() || line.startsWith("WEBVTT")) {
                        continue;
                    }
                    isHeader = false;
                }
                
                // Skip empty lines
                if (line.isEmpty()) {
                    if (currentSegment != null && isInSegment) {
                        currentSegment.setText(textBuilder.toString().trim());
                        segments.add(currentSegment);
                        currentSegment = null;
                        textBuilder = new StringBuilder();
                        isInSegment = false;
                    }
                    continue;
                }
                
                // Parse timestamp
                if (line.contains("-->")) {
                    currentSegment = new TranscriptSegment();
                    String[] times = line.split("-->");
                    String startTimeStr = times[0].trim();
                    String endTimeStr = times[1].trim();
                    
                    // Remove any VTT positioning/styling info (everything after the timestamp)
                    startTimeStr = startTimeStr.split("\\s+")[0];
                    endTimeStr = endTimeStr.split("\\s+")[0];
                    
                    // Convert VTT time format (HH:MM:SS.mmm) to seconds
                    currentSegment.setStartTime(convertTimeToSeconds(startTimeStr, '.'));
                    currentSegment.setEndTime(convertTimeToSeconds(endTimeStr, '.'));
                    isInSegment = true;
                    continue;
                }
                
                // Add text to current segment
                if (currentSegment != null && isInSegment) {
                    if (textBuilder.length() > 0) {
                        textBuilder.append(" ");
                    }
                    textBuilder.append(line);
                }
            }
            
            // Add the last segment if exists
            if (currentSegment != null && isInSegment) {
                currentSegment.setText(textBuilder.toString().trim());
                segments.add(currentSegment);
            }
        } catch (Exception e) {
            System.out.println("Error reading file: " + e.getMessage());
            throw new IOException("Error reading transcript file: " + e.getMessage(), e);
        } finally {
            // Clean up the temporary directory and its contents
            if (tempDir.exists()) {
                File[] files = tempDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        file.delete();
                    }
                }
                tempDir.delete();
            }
        }
        
        if (segments.isEmpty()) {
            throw new IOException("No transcript segments were found in the file");
        }
        
        return segments;
    }
    
    private double convertTimeToSeconds(String timeStr, char millisSeparator) {
        // Remove any positioning/styling info that might be appended (like "align:start", "line:869", etc.)
        timeStr = timeStr.split("\\s+")[0]; // Take only the first part (the actual timestamp)
        
        // Format: HH:MM:SS,mmm or HH:MM:SS.mmm
        String[] parts = timeStr.split("[:\\" + millisSeparator + "]");
        
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid time format: " + timeStr);
        }
        
        try {
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            int seconds = Integer.parseInt(parts[2]);
            int milliseconds = Integer.parseInt(parts[3]);
            
            return hours * 3600 + minutes * 60 + seconds + milliseconds / 1000.0;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Could not parse time components from: " + timeStr, e);
        }
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
