package com.vidhuratech.jobs.common.service;

import org.springframework.stereotype.Service;

@Service
public class OTPEmailTemplateService {

    public String buildOtpEmailTemplate(String name, String otp) {

        return """
    <div style="background:#f4f7fb;padding:30px;font-family:'Segoe UI',Arial,sans-serif">

        <div style="max-width:600px;margin:auto;background:#ffffff;border-radius:12px;
                    box-shadow:0 10px 30px rgba(0,0,0,0.08);overflow:hidden">

            <!-- HEADER -->
            <div style="background:linear-gradient(135deg,#0d223f,#122b4f);
                        padding:20px;text-align:center">

                    <img src="cid:logoImage" alt="Vidhura Tech" style="height:60px;margin-bottom:10px"/>

                <h2 style="color:#ffffff;margin:0;font-weight:700">
                    Vidhura Tech
                </h2>

                <p style="color:#cbd5e1;margin:5px 0 0">
                    Code Your Future 🚀
                </p>
            </div>

            <!-- BODY -->
            <div style="padding:30px;text-align:center">

                <h3 style="color:#111827;margin-bottom:10px">
                    🔐 Your One-Time Password
                </h3>

                <p style="color:#6b7280;font-size:15px">
                    Hello <strong>%s</strong>,
                </p>

                <p style="color:#6b7280;font-size:14px">
                    Use the OTP below to securely login to your account.
                </p>

                <!-- OTP BOX -->
                <div style="
                    margin:25px auto;
                    padding:15px 25px;
                    background:#f1f5f9;
                    border-radius:10px;
                    display:inline-block;
                    font-size:28px;
                    letter-spacing:4px;
                    font-weight:bold;
                    color:#2563eb;
                ">
                    %s
                </div>

                <p style="color:#ef4444;font-size:13px;margin-top:10px">
                    ⏱️ Valid for 5 minutes
                </p>

                <p style="color:#9ca3af;font-size:12px;margin-top:20px">
                    If you didn’t request this, please ignore this email.
                </p>

            </div>

            <!-- FOOTER -->
            <div style="background:#f9fafb;padding:20px;text-align:center;
                        border-top:1px solid #e5e7eb">

                <p style="margin:0;font-size:13px;color:#6b7280">
                    © 2026 Vidhura Tech. All rights reserved.
                </p>

                <p style="margin:5px 0 0;font-size:12px;color:#9ca3af">
                    support@vidhuratech.com
                </p>

            </div>

        </div>

    </div>
    """.formatted(name, otp);
    }
}
