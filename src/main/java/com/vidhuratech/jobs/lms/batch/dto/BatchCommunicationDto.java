package com.vidhuratech.jobs.lms.batch.dto;

import lombok.Data;

@Data
public class BatchCommunicationDto {
    private String whatsappGroupLink;
    private String zoomJoinLink;
    private String zoomMeetingId;
    private String zoomPasscode;
    private String zoomSchedule;
    private String zoomTime;
    private String zoomCalendarLink;
}
