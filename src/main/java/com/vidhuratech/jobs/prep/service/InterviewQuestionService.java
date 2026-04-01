package com.vidhuratech.jobs.prep.service;

import com.vidhuratech.jobs.prep.entity.InterviewQuestion;
import com.vidhuratech.jobs.prep.repository.InterviewQuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InterviewQuestionService {

    @Autowired
    private InterviewQuestionRepository repo;

    public Page<InterviewQuestion> getQuestions(
            String company,
            String role,
            String search,
            String type,
            String difficulty,
            String topic,
            int page
    ) {

        Pageable pageable = PageRequest.of(page, 10);

        return repo.filterQuestions(
                company,
                role,
                normalize(search),
                normalize(type),
                normalize(difficulty),
                normalize(topic),
                pageable
        );    }

    public void saveAll(List<InterviewQuestion> list) {
        repo.saveAll(list);
    }

    private String normalize(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }}
