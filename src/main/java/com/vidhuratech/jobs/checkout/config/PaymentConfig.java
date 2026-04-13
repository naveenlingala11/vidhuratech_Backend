package com.vidhuratech.jobs.checkout.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "payment")
@Data
public class PaymentConfig {

    private String upiId;
    private String merchantName;
}