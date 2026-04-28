package com.vidhuratech.jobs.common.controller;

import com.vidhuratech.jobs.common.dto.ContactRequest;
import com.vidhuratech.jobs.common.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
@CrossOrigin("*")
public class PublicContactController {

    private final EmailService emailService;

    @PostMapping("/contact")
    public ResponseEntity<?> sendContactEmail(@RequestBody ContactRequest request) {

        String subject = "New Contact Enquiry - Vidhura Tech";

        String html = """
                <div style="font-family:Arial,sans-serif;background:#f4f7fb;padding:25px">
                    <div style="max-width:650px;margin:auto;background:#ffffff;border-radius:12px;
                                overflow:hidden;box-shadow:0 8px 25px rgba(0,0,0,0.08)">
                        
                        <div style="background:#0d223f;padding:22px;text-align:center">
                            <h2 style="color:#ffffff;margin:0">New Contact Enquiry</h2>
                            <p style="color:#cbd5e1;margin:6px 0 0">Vidhura Tech Website</p>
                        </div>

                        <div style="padding:28px">
                            <p style="font-size:15px;color:#374151">
                                You received a new message from the contact page.
                            </p>

                            <table style="width:100%%;border-collapse:collapse;margin-top:18px">
                                <tr>
                                    <td style="padding:12px;border:1px solid #e5e7eb;font-weight:bold">Name</td>
                                    <td style="padding:12px;border:1px solid #e5e7eb">%s</td>
                                </tr>
                                <tr>
                                    <td style="padding:12px;border:1px solid #e5e7eb;font-weight:bold">Email</td>
                                    <td style="padding:12px;border:1px solid #e5e7eb">%s</td>
                                </tr>
                                <tr>
                                    <td style="padding:12px;border:1px solid #e5e7eb;font-weight:bold">Phone</td>
                                    <td style="padding:12px;border:1px solid #e5e7eb">%s</td>
                                </tr>
                                <tr>
                                    <td style="padding:12px;border:1px solid #e5e7eb;font-weight:bold">Message</td>
                                    <td style="padding:12px;border:1px solid #e5e7eb;line-height:1.6">%s</td>
                                </tr>
                            </table>
                        </div>
                    </div>
                </div>
                """.formatted(
                request.getName(),
                request.getEmail(),
                request.getPhone(),
                request.getMessage()
        );

        emailService.sendHtmlEmail(
                "support@vidhuratech.com",
                subject,
                html
        );

        return ResponseEntity.ok(Map.of("message", "Contact message sent successfully"));
    }
}
