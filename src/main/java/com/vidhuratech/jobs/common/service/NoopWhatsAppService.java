package com.vidhuratech.jobs.common.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnMissingBean(name = "twilioWhatsAppService")
@Slf4j
public class NoopWhatsAppService implements WhatsAppService {
    @Override
    public void sendText(String phone, String message) {
        log.info("WhatsApp disabled. phone={}, message={}", phone, message);
    }
}
