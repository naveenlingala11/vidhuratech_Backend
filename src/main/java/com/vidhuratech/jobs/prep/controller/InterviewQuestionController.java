package com.vidhuratech.jobs.prep.controller;

import com.vidhuratech.jobs.prep.entity.InterviewQuestion;
import com.vidhuratech.jobs.prep.service.InterviewQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@CrossOrigin("*")
public class InterviewQuestionController {

    @Autowired
    private InterviewQuestionService service;

    @GetMapping
    public Page<InterviewQuestion> getQuestions(
            @RequestParam String company,
            @RequestParam String role,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String topic,
            @RequestParam(defaultValue = "0") int page
    ) {
        return service.getQuestions(company, role, search, type, difficulty, topic, page);
    }

    @PostMapping("/bulk")
    public String uploadQuestions(@RequestBody List<InterviewQuestion> questions) {

        service.saveAll(questions);

        return "Saved Successfully";
    }

}