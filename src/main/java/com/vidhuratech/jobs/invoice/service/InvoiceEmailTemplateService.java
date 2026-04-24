package com.vidhuratech.jobs.invoice.service;

import com.vidhuratech.jobs.invoice.entity.Invoice;
import org.springframework.stereotype.Service;

@Service
public class InvoiceEmailTemplateService {

    public String buildPremiumInvoiceEmail(Invoice invoice) {

        return """
        <div style="font-family:'Segoe UI',Arial;background:#f4f6f8;padding:40px">

            <div style="max-width:700px;margin:auto;background:#ffffff;border-radius:16px;padding:40px">

                <h2 style="color:#0d6efd;">🎉 Payment Successful</h2>

                <p style="font-size:16px;">
                    Hi <b>%s</b>,
                </p>

                <p style="color:#555;">
                    Welcome to <b>Vidhura Tech</b> 🚀 <br>
                    Your payment has been successfully processed and your enrollment is now confirmed.
                </p>

                <hr style="margin:25px 0;">

                <h3 style="color:#0f172a;">📄 Invoice Details</h3>

                <table style="width:100%%;border-collapse:collapse;font-size:14px">
                    <tr><td><b>Invoice ID</b></td><td>%s</td></tr>
                    <tr><td><b>Course</b></td><td>%s</td></tr>
                    <tr><td><b>Batch</b></td><td>%s</td></tr>
                    <tr>
                        <td><b>Amount Paid</b></td>
                        <td style="color:#16a34a;"><b>₹%.2f</b></td>
                    </tr>
                    <tr>
                        <td><b>Status</b></td>
                        <td style="color:green;">PAID</td>
                    </tr>
                </table>

                <hr style="margin:25px 0;">

                <h3>🚀 What Happens Next?</h3>

                <ul style="color:#555;line-height:1.8;">
                    <li>
                        👉 Join WhatsApp Group: 
                        <a href="https://chat.whatsapp.com/GAkHYqCG9ScBH8hQAUHkzt" 
                           style="color:#0d6efd;text-decoration:none;">
                           Click here to join
                        </a>
                    </li>

                    <li>📚 You will receive course access details shortly</li>

                    <li>
                        🎥 Zoom / Live session invite link will be shared 
                        <b>1 day before the class starts</b>
                    </li>

                    <li>🧠 Attend live sessions and clarify doubts</li>

                    <li>💻 Start building real-time projects</li>
                </ul>

                <hr style="margin:25px 0;">

                <p style="color:#555;">
                    If you have any questions or need assistance, feel free to reach out anytime.<br><br>

                    📧 support@vidhuratech.com<br>
                    📞 +91 9108057464
                </p>

                <p style="margin-top:30px;">
                    Regards,<br>
                    <b>Team Vidhura Tech 💙</b>
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