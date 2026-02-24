package com.example.back.controller;

import com.example.back.dto.IndexDto;
import com.example.back.dto.NewsDto;
import com.example.back.service.BriefingService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/briefing")
@CrossOrigin(origins = "*")
public class BriefingController {

    private final BriefingService briefingService;

    public BriefingController(BriefingService briefingService) {
        this.briefingService = briefingService;
    }

    @GetMapping("/indices")
    public List<IndexDto> getIndices() {
        return briefingService.getIndices();
    }

    @GetMapping("/news")
    public List<NewsDto> getNews() {
        return briefingService.getNews();
    }

    @GetMapping("/summary")
    public List<String> getSummary() {
        return briefingService.getAiSummary();
    }
}
