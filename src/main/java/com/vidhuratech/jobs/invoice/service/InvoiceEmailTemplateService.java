package com.vidhuratech.jobs.invoice.service;

import com.vidhuratech.jobs.invoice.entity.Invoice;
import org.springframework.stereotype.Service;

@Service
public class InvoiceEmailTemplateService {

    public String buildWelcomeInvoiceEmail(Invoice invoice) {

        return """
            <div style='font-family:Arial;padding:30px;background:#f8fafc'>
                <div style='max-width:700px;margin:auto;background:#fff;padding:40px;border-radius:16px'>
                    
                    <h1 style='color:#0f172a;'>Welcome to Vidhura Tech 🚀</h1>

                    <p>Hi <b>%s</b>,</p>

                    <p>
                        Thank you for trusting <b>Vidhura Tech</b> and enrolling in our program.
                        We’re excited to be part of your learning journey.
                    </p>

                    <p>
                        Your payment has been successfully verified and your access has been activated.
                    </p>

                    <hr>

                    <h3>Invoice Details</h3>

                    <table style='width:100%%;border-collapse:collapse'>
                        <tr><td><b>Invoice ID</b></td><td>%s</td></tr>
                        <tr><td><b>Course</b></td><td>%s</td></tr>
                        <tr><td><b>Batch</b></td><td>%s</td></tr>
                        <tr><td><b>Amount Paid</b></td><td>₹%.2f</td></tr>
                        <tr><td><b>Payment Status</b></td><td>PAID</td></tr>
                    </table>

                    <hr>

                    <p>
                        <b>What’s Next?</b><br>
                        • Join WhatsApp Group<br>
                        • Access Student Dashboard<br>
                        • Attend Live Sessions<br>
                        • Start Building Projects
                    </p>

                    <br>

                    <p>
                        We sincerely appreciate your trust in us.<br>
                        Let’s build your career together 💙
                    </p>

                    <br>

                    <p>
                        Regards,<br>
                        <b>Team Vidhura Tech</b><br>
                        support@vidhuratech.com
                    </p>

                </div>
            </div>
        """.formatted(
                invoice.getName(),
                invoice.getId(),
                invoice.getCourse(),
                invoice.getBatch(),
                invoice.getAmount()
        );
    }
}