package com.vidhuratech.jobs.common.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/zoho")
public class ZohoAuthController {

    @GetMapping("/callback")
    public String callback(@RequestParam(required = false) String code) {

        if (code == null) {
            return "Authorization failed. No code received.";
        }

        return "Authorization successful. Code received.";
    }
}