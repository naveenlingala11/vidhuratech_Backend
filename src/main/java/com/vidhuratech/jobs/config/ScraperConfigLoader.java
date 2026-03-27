package com.vidhuratech.jobs.config;

import com.vidhuratech.jobs.scraper.engine.ApiConfig;
import com.vidhuratech.jobs.entity.*;
import com.vidhuratech.jobs.repository.*;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ScraperConfigLoader {

    private final ScraperConfigRepository repo;

    public ScraperConfigLoader(ScraperConfigRepository repo) {
        this.repo = repo;
    }

    @PostConstruct
    public void load() {

        // 🔥 already exists → skip
        if (repo.count() > 50) {
            System.out.println("✅ Companies already loaded");
            return;
        }

        System.out.println("🚀 Loading companies into DB...");

        List<ApiConfig> configs = verifiedConfigs();

        int added = 0;

        for (ApiConfig c : configs) {
            try {

                boolean exists = repo.findAll().stream()
                        .anyMatch(e -> e.getCompany().equalsIgnoreCase(c.getCompany()));

                if (exists) continue;

                ScraperConfigEntity e = new ScraperConfigEntity();
                e.setCompany(c.getCompany());
                e.setType(c.getType());
                e.setUrl(c.getUrl());

                repo.save(e);
                added++;

            } catch (Exception ex) {
                System.out.println("❌ Failed: " + c.getCompany());
            }
        }

        System.out.println("✅ TOTAL ADDED: " + added);
    }

    private List<ApiConfig> verifiedConfigs() {

        List<ApiConfig> list = new ArrayList<>();

        // ───────────────── GREENHOUSE (80+) ─────────────────
        List<String> gh = List.of(
                "postman","notion","coinbase","robinhood","figma","brex","plaid","stripe",
                "doordash","lyft","dropbox","asana","zendesk","twilio","cloudflare",
                "datadog","hashicorp","pagerduty","okta","newrelic","grafanalabs",
                "1password","airtable","amplitude","intercom","mixpanel","netlify",
                "vercel","linear","segment",
                "instacart","discord","shopify","reddit","coursera","quora",
                "atlassian","canva","unity","zapier","loom","calendly",
                "drift","gusto","figment","retool","scaleai","openai",
                "grammarly","udemy","robinhood","brex","rippling","webflow",
                "superhuman","flexport","figma","clearbit","segment",
                "sendgrid","fastly","algolia","snyk","circleci",
                "contentful","gitlab","elastic","docker","digitalocean",
                "hashicorp","mongodb","snowflake","databricks"
        );

        gh.forEach(s -> list.add(gh(cap(s), s)));

        // ───────────────── LEVER (60+) ─────────────────
        List<String> lv = List.of(
                "canva","rippling","scaleai","faire","benchling","lattice","gem","persona",
                "retool","dbtlabs","airbyte","census","hightouch","temporal","hex",
                "montecarlodata","prefect","dagsterlabs","astronomer","observeinc",
                "zapier","figment","pilot","ramp","bolt","clerk",
                "supabase","planetscale","railway","render","flyio",
                "replicate","modal","together","perplexity","groq",
                "runwayml","elevenlabs","pika","replit","codeium",
                "vanta","merge","checkr","veriff","sift",
                "deel","remote","multiplier","rippling"
        );

        lv.forEach(s -> list.add(lv(cap(s), s)));

        // ───────────────── WORKDAY (REAL INDIA) ─────────────────
        list.addAll(List.of(
                wd("Deloitte","https://deloitte.wd1.myworkdayjobs.com/wday/cxs/deloitte/DeloitteCareers/jobs"),
                wd("EY","https://ey.wd5.myworkdayjobs.com/wday/cxs/ey/EY_External_Careers/jobs"),
                wd("KPMG","https://kpmg.wd3.myworkdayjobs.com/wday/cxs/kpmg/KPMG_External/jobs"),
                wd("Accenture","https://accenture.wd3.myworkdayjobs.com/wday/cxs/accenture/AccentureCareers/jobs"),
                wd("Capgemini","https://capgemini.wd3.myworkdayjobs.com/wday/cxs/capgemini/Capgemini_Careers/jobs"),
                wd("Wipro","https://wipro.wd3.myworkdayjobs.com/wday/cxs/wipro/WiproExternalCareerSite/jobs"),
                wd("HCL","https://hcltech.wd3.myworkdayjobs.com/wday/cxs/hcltech/HCLTechCareers/jobs"),
                wd("Infosys","https://infosys.wd5.myworkdayjobs.com/wday/cxs/infosys/InfosysCareers/jobs"),
                wd("IBM","https://ibm.wd5.myworkdayjobs.com/wday/cxs/ibm/External/jobs"),
                wd("Oracle","https://oracle.wd1.myworkdayjobs.com/wday/cxs/oracle/External/jobs"),
                wd("Cisco","https://cisco.wd1.myworkdayjobs.com/wday/cxs/cisco/jobs/jobs"),
                wd("ServiceNow","https://servicenow.wd5.myworkdayjobs.com/wday/cxs/servicenow/jobs/jobs")
        ));

        // ───────────────── SMARTRECRUITERS ─────────────────
        List<String> sr = List.of(
                "Amazon","Microsoft","Google","Adobe","SAP",
                "Siemens","BoschGroup","Philips","Ericsson","Nokia",
                "ABB","SchneiderElectric","Honeywell","Amdocs"
        );

        sr.forEach(s -> list.add(sr(s, s)));

        return list;
    }

    // ── HELPERS ──
    private String cap(String s) {
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }

    private ApiConfig gh(String c, String s) {
        return cfg(c,"greenhouse",
                "https://boards-api.greenhouse.io/v1/boards/"+s+"/jobs?content=true");
    }

    private ApiConfig lv(String c, String s) {
        return cfg(c,"lever",
                "https://api.lever.co/v0/postings/"+s+"?mode=json");
    }

    private ApiConfig wd(String c, String url) {
        return cfg(c,"workday",url);
    }

    private ApiConfig sr(String c, String id) {
        return cfg(c,"smartrecruiters",
                "https://api.smartrecruiters.com/v1/companies/"+id+"/postings");
    }

    private ApiConfig cfg(String c, String t, String u) {
        ApiConfig a = new ApiConfig();
        a.setCompany(c);
        a.setType(t);
        a.setUrl(u);
        return a;
    }
}