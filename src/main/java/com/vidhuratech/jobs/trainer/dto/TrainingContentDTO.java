package com.vidhuratech.jobs.trainer.dto;

import com.vidhuratech.jobs.trainer.entity.TrainingContent;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TrainingContentDTO {
    private Long id;
    private Long batchId;
    private String type;
    private String title;
    private String description;
    private String fileName;
    private String fileType;
    private LocalDateTime createdAt;
    private String jsonData;
    private String links;

    public static TrainingContentDTO from(TrainingContent content) {
        return TrainingContentDTO.builder()
                .id(content.getId())
                .batchId(content.getBatchId())
                .type(content.getType() != null ? content.getType().name() : null)
                .title(content.getTitle())
                .description(content.getDescription())
                .fileName(content.getFileName())
                .fileType(content.getFileType())
                .createdAt(content.getCreatedAt())
                .jsonData(content.getJsonData())
                .links(content.getLinks())
                .build();
    }
}