package com.vidhuratech.jobs.invoice.service;

import com.vidhuratech.jobs.invoice.entity.Invoice;
import org.springframework.stereotype.Service;

@Service
public class InvoiceEmailTemplateService {

    public String buildPremiumInvoiceEmail(Invoice invoice) {

        return """
<div style="font-family:'Segoe UI',Arial;background:#f1f5f9;padding:30px">

    <!-- MAIN CARD -->
    <div style="max-width:720px;margin:auto;background:#ffffff;border-radius:18px;overflow:hidden;
                box-shadow:0 20px 50px rgba(0,0,0,0.08)">

        <!-- HEADER -->
        <div style="background:linear-gradient(135deg,#2563eb,#16a34a);
                                    padding:30px;
                                    color:#fff;
                                    text-align:center">
                
                            <!-- LOGO -->
                            <img src="https://www.vidhuratech.com/VidhuraTechLogo.png"
                                 alt="Vidhura Tech"
                                 style="width:120px;
                                        margin-bottom:12px;
                                        border-radius:12px;
                                        background:#fff;
                                        padding:6px;
                                        box-shadow:0 6px 18px rgba(0,0,0,0.2);"/>
                
                            <h1 style="margin:0;font-size:26px;">🎉 Payment Successful</h1>
                
                            <p style="margin-top:8px;font-size:14px;opacity:.9">
                                Welcome to Vidhura Tech 🚀
                            </p>
                
                        </div>

        <!-- BODY -->
        <div style="padding:32px">

            <p style="font-size:16px;margin-bottom:10px">
                Hi <b>%s</b>,
            </p>

            <p style="color:#475569;margin-bottom:25px">
                Your payment has been successfully processed and your enrollment is now confirmed.
            </p>

            <!-- INVOICE CARD -->
            <div style="border:1px solid #e2e8f0;border-radius:14px;padding:20px;margin-bottom:25px">
                <h3 style="margin-top:0;color:#0f172a;">📄 Invoice Details</h3>

                <table style="width:100%%;font-size:14px;color:#334155">
                    <tr><td>Invoice ID</td><td><b>%s</b></td></tr>
                    <tr><td>Course</td><td><b>%s</b></td></tr>
                    <tr><td>Batch</td><td><b>%s</b></td></tr>
                    <tr>
                        <td>Amount Paid</td>
                        <td style="color:#16a34a;font-weight:700">₹%.2f</td>
                    </tr>
                    <tr>
                        <td>Status</td>
                        <td style="color:#16a34a;font-weight:700">PAID ✅</td>
                    </tr>
                </table>
            </div>

            <!-- NEXT STEPS -->
            <h3 style="color:#0f172a;">🚀 What Happens Next?</h3>

            <!-- WhatsApp -->
            <div style="background:#ecfdf5;border:1px solid #bbf7d0;padding:16px;border-radius:12px;margin-bottom:12px">
                <b>📲 Join WhatsApp Community</b><br><br>
                <a href="https://chat.whatsapp.com/GAkHYqCG9ScBH8hQAUHkzt"
                   style="background:#16a34a;color:#fff;padding:10px 16px;border-radius:8px;
                          text-decoration:none;font-weight:600;display:inline-block">
                   Join Now →
                </a>
            </div>

            <!-- LMS -->
            <div style="background:#eff6ff;border:1px solid #bfdbfe;padding:16px;border-radius:12px;margin-bottom:12px">
                📚 Your course access will be activated shortly
            </div>

            <!-- Zoom -->
            <div style="background:#fef9c3;border:1px solid #fde68a;padding:16px;border-radius:12px;margin-bottom:12px">
                🎥 Live class details are available below 👇
            </div>

            <!-- Learning -->
            <div style="background:#f1f5f9;border:1px solid #e2e8f0;padding:16px;border-radius:12px;margin-bottom:12px">
                🧠 Attend live sessions & clarify doubts
            </div>

            <!-- Projects -->
            <div style="background:#faf5ff;border:1px solid #e9d5ff;padding:16px;border-radius:12px;margin-bottom:20px">
                💻 Build real-time projects & grow your career 🚀
            </div>

            <!-- ZOOM CARD -->
            <div style="border-radius:16px;
                        padding:22px;
                        background:linear-gradient(135deg,#eff6ff,#ecfdf5);
                        border:1px solid #dbeafe;
                        margin-bottom:25px">

                <h3 style="margin-top:0">🎥 Live Class Details</h3>

                <p><b>📘 Course:</b> Python + Data Structures</p>
                <p><b>📅 Start:</b> May 2, 2026</p>
                <p><b>⏰ Time:</b> 07:30 PM IST</p>

                <!-- JOIN BUTTON -->
                <div style="margin:15px 0">
                    <a href="https://us06web.zoom.us/j/87114375458?pwd=k84SGuA0a0mXw6eFCoYtJcpjRbs9eo.1"
                       style="background:#2563eb;color:#fff;padding:12px 18px;border-radius:10px;
                              text-decoration:none;font-weight:700;display:inline-block">
                       🚀 Join Live Class
                    </a>
                </div>

                <p style="font-size:13px;color:#475569">
                    🆔 Meeting ID: 871 1437 5458 <br>
                    🔐 Passcode: 278110
                </p>

                <!-- CALENDAR -->
                <a href="https://us06web.zoom.us/meeting/tZMsdu2rqT4uH9wgCVQjq2RlbX7_3dawgTp3/ics?icsToken=DPALmQahb4E01csChQAALAAAACKFzMAj3zx_O5HRcVsP2s5X6F2GSuk3BLGJ5ArZWHx1ZjNnXBjrcwIsTLBfsgMWkogycBLFdL6QRoEzIzAwMDAwMQ&meetingMasterEventId=MkicPAKwR2utjCV-G7HViQ"
                   style="background:#16a34a;color:#fff;padding:8px 14px;border-radius:8px;
                          text-decoration:none;font-weight:600;display:inline-block">
                   📅 Add to Calendar
                </a>

            </div>

        </div>

        <!-- FOOTER -->
        <div style="background:#0f172a;color:#cbd5e1;padding:25px;text-align:center">

            <p style="margin:0 0 10px 0;font-weight:600">
                Need help? We're here for you 💙
            </p>

            <p style="margin:0;font-size:14px">
                📧 support@vidhuratech.com <br>
                📞 +91 9108057464
            </p>

            <p style="margin-top:15px;font-size:12px;opacity:.7">
                © 2026 Vidhura Tech. All rights reserved.
            </p>

        </div>

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