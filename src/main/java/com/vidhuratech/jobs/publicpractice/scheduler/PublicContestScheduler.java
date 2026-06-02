package com.vidhuratech.jobs.publicpractice.scheduler;

import com.vidhuratech.jobs.publicpractice.service.PublicPracticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PublicContestScheduler {

    private final PublicPracticeService publicPracticeService;

    @Scheduled(cron = "0 0 9 ? * MON", zone = "Asia/Kolkata")
    public void announceWeeklyTopThree() {
        publicPracticeService.publishWeeklyTopThreeAnnouncement();
    }
}