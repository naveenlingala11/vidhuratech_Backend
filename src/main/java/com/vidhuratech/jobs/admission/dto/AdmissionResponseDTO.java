package com.vidhuratech.jobs.admission.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdmissionResponseDTO {

    private boolean studentCreated;

    private boolean existingStudent;

    private boolean enrollmentCreated;

    private boolean invoiceGenerated;

    private boolean setupPasswordMailSent;

    private String setupPasswordStatus;

    private String studentEmail;

    private String temporaryPassword;

    private String nextStep;
}