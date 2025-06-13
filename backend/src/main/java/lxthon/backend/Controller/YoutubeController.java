package lxthon.backend.Controller;

import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lxthon.backend.Service.YoutubeService;

@RestController
@RequestMapping("/api/videos")
public class YoutubeController {
    
    private final YoutubeService youtubeService;
    
    public YoutubeController(YoutubeService youtubeService) {
        this.youtubeService = youtubeService;
    }
    
    @GetMapping("/info")
    public String getVideoInfo(@RequestParam String url) throws IOException, InterruptedException {
        return youtubeService.getVideoInfo(url);
    }
    
    @GetMapping("/download")
    public String downloadVideo(
            @RequestParam String url,
            @RequestParam(required = false) String format) throws IOException, InterruptedException {
        return youtubeService.downloadVideo(url, format);
    }
    
    @GetMapping("/playlist")
    public String downloadPlaylist(
            @RequestParam String url,
            @RequestParam(required = false) String format) throws IOException, InterruptedException {
        return youtubeService.downloadPlaylist(url, format);
    }
}

