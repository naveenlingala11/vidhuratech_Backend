package com.vidhuratech.jobs.jobs.config;

import com.vidhuratech.jobs.jobs.entity.ScraperConfigEntity;
import com.vidhuratech.jobs.jobs.repository.ScraperConfigRepository;
import com.vidhuratech.jobs.jobs.scraper.engine.ApiConfig;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
public class ScraperConfigLoader {

    private final ScraperConfigRepository repo;

    public ScraperConfigLoader(ScraperConfigRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public void load() {
        System.out.println("🚀 Refreshing/Loading scraper configurations into DB...");
        try {
            List<ScraperConfigEntity> existingEntities = repo.findAll();
            List<ApiConfig> verifiedConfigs = verifiedConfigs();

            // Map existing configurations by "company:type" key
            Map<String, ScraperConfigEntity> existingMap = new HashMap<>();
            for (ScraperConfigEntity entity : existingEntities) {
                String key = (entity.getCompany() + ":" + entity.getType()).toLowerCase();
                existingMap.put(key, entity);
            }

            Set<String> verifiedKeys = new HashSet<>();
            int updated = 0;
            int created = 0;

            for (ApiConfig c : verifiedConfigs) {
                String key = (c.getCompany() + ":" + c.getType()).toLowerCase();
                verifiedKeys.add(key);

                ScraperConfigEntity entity = existingMap.get(key);
                if (entity != null) {
                    // Update URL if changed
                    if (!c.getUrl().equals(entity.getUrl())) {
                        entity.setUrl(c.getUrl());
                        repo.save(entity);
                        updated++;
                    }
                } else {
                    // Create new configuration
                    ScraperConfigEntity newEntity = new ScraperConfigEntity();
                    newEntity.setCompany(c.getCompany());
                    newEntity.setType(c.getType());
                    newEntity.setUrl(c.getUrl());
                    newEntity.setActive(true);
                    newEntity.setFailCount(0);
                    newEntity.setSuccessCount(0);
                    repo.save(newEntity);
                    created++;
                }
            }

            System.out.println("✅ Loaded verified scraper configurations: " + created + " created, " + updated + " updated.");
        } catch (Exception ex) {
            System.out.println("❌ Failed to load configurations: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private List<ApiConfig> verifiedConfigs() {
        List<ApiConfig> list = new ArrayList<>();

        // ───────────────── GREENHOUSE (Verified India offices / Indian Tech) ─────────────────
        list.add(gh("Razorpay", "razorpaysoftwareprivatelimited"));
        list.add(gh("Groww", "groww"));
        list.add(gh("InMobi", "inmobi"));
        list.add(gh("Delhivery", "delhivery"));
        list.add(gh("Cars24", "cars24"));
        list.add(gh("ShareChat", "sharechat"));
        list.add(gh("Urban Company", "urbancompany"));
        list.add(gh("Pocket FM", "pocketfm"));
        list.add(gh("Postman", "postman"));
        list.add(gh("Atlassian", "atlassian"));
        list.add(gh("Stripe", "stripe"));
        list.add(gh("Coinbase", "coinbase"));
        list.add(gh("Vercel", "vercel"));
        list.add(gh("Elastic", "elastic"));
        list.add(gh("MongoDB", "mongodb"));
        list.add(gh("Snowflake", "snowflake"));
        list.add(gh("Databricks", "databricks"));
        list.add(gh("Twilio", "twilio"));
        list.add(gh("New Relic", "newrelic"));
        list.add(gh("GitLab", "gitlab"));
        list.add(gh("Docker", "docker"));
        list.add(gh("DigitalOcean", "digitalocean"));
        list.add(gh("PagerDuty", "pagerduty"));

        // ───────────────── LEVER (Verified) ─────────────────
        list.add(lv("Meesho", "meesho"));
        list.add(lv("CRED", "cred"));
        list.add(lv("Zeta", "zeta"));
        list.add(lv("Rippling", "rippling"));
        list.add(lv("Deel", "deel"));
        list.add(lv("Benchling", "benchling"));
        list.add(lv("Vanta", "vanta"));
        list.add(lv("Replit", "replit"));

        // ───────────────── WORKDAY (MNC India / Indian giants) ─────────────────
        // Commented out due to internal play session CSRF protection causing 422 responses
        // list.add(wd("Deloitte", "https://deloitte.wd1.myworkdayjobs.com/wday/cxs/deloitte/DeloitteCareers/jobs"));
        // list.add(wd("EY", "https://ey.wd5.myworkdayjobs.com/wday/cxs/ey/EY_External_Careers/jobs"));
        // list.add(wd("KPMG", "https://kpmg.wd3.myworkdayjobs.com/wday/cxs/kpmg/KPMG_External/jobs"));
        // list.add(wd("Accenture", "https://accenture.wd3.myworkdayjobs.com/wday/cxs/accenture/AccentureCareers/jobs"));
        // list.add(wd("Capgemini", "https://capgemini.wd3.myworkdayjobs.com/wday/cxs/capgemini/Capgemini_Careers/jobs"));
        // list.add(wd("Wipro", "https://wipro.wd3.myworkdayjobs.com/wday/cxs/wipro/WiproExternalCareerSite/jobs"));
        // list.add(wd("HCL", "https://hcltech.wd3.myworkdayjobs.com/wday/cxs/hcltech/HCLTechCareers/jobs"));
        // list.add(wd("Infosys", "https://infosys.wd5.myworkdayjobs.com/wday/cxs/infosys/InfosysCareers/jobs"));
        // list.add(wd("IBM", "https://ibm.wd5.myworkdayjobs.com/wday/cxs/ibm/External/jobs"));
        // list.add(wd("Oracle", "https://oracle.wd1.myworkdayjobs.com/wday/cxs/oracle/External/jobs"));
        // list.add(wd("Cisco", "https://cisco.wd1.myworkdayjobs.com/wday/cxs/cisco/jobs/jobs"));
        // list.add(wd("ServiceNow", "https://servicenow.wd5.myworkdayjobs.com/wday/cxs/servicenow/jobs/jobs"));
        // list.add(wd("PhonePe", "https://phonepe.wd3.myworkdayjobs.com/wday/cxs/phonepe/PhonePe_Careers/jobs"));
        // list.add(wd("Swiggy", "https://swiggy.wd3.myworkdayjobs.com/wday/cxs/swiggy/Swiggy_Careers/jobs"));
        // list.add(wd("Flipkart", "https://flipkart.wd3.myworkdayjobs.com/wday/cxs/flipkart/Flipkart_Careers/jobs"));

        // ───────────────── SMARTRECRUITERS (Verified) ─────────────────
        list.add(sr("BoschGroup", "BoschGroup"));
        list.add(sr("Siemens", "Siemens"));
        list.add(sr("Philips", "Philips"));
        list.add(sr("Ericsson", "Ericsson"));
        list.add(sr("Nokia", "Nokia"));
        list.add(sr("ABB", "ABB"));
        list.add(sr("Honeywell", "Honeywell"));
        list.add(sr("Amdocs", "Amdocs"));

        return list;
    }

    // ── HELPERS ──
    private ApiConfig gh(String c, String s) {
        return cfg(c, "greenhouse",
                "https://boards-api.greenhouse.io/v1/boards/" + s + "/jobs?content=true");
    }

    private ApiConfig lv(String c, String s) {
        return cfg(c, "lever",
                "https://api.lever.co/v0/postings/" + s + "?mode=json");
    }

    private ApiConfig wd(String c, String url) {
        return cfg(c, "workday", url);
    }

    private ApiConfig sr(String c, String id) {
        return cfg(c, "smartrecruiters",
                "https://api.smartrecruiters.com/v1/companies/" + id + "/postings");
    }

    private ApiConfig cfg(String c, String t, String u) {
        ApiConfig a = new ApiConfig();
        a.setCompany(c);
        a.setType(t);
        a.setUrl(u);
        return a;
    }
}