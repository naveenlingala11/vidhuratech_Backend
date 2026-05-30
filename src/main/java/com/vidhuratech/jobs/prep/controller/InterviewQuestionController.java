package com.vidhuratech.jobs.prep.controller;

import com.vidhuratech.jobs.prep.service.InterviewQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/questions")
@CrossOrigin("*")
@RequiredArgsConstructor
public class InterviewQuestionController {

    private final InterviewQuestionService service;

    @GetMapping
    public Page<Map<String, Object>> getQuestions(
            @RequestParam(defaultValue = "") String company,
            @RequestParam(defaultValue = "") String role,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String type,
            @RequestParam(defaultValue = "") String difficulty,
            @RequestParam(defaultValue = "") String topic,
            @RequestParam(defaultValue = "0") int page
    ) {
        return service.getQuestions(company, role, search, type, difficulty, topic, page);
    }
}