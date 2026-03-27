package com.vidhuratech.jobs.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class JobEnrichmentService {

    // ── Called by JobService to set category on each saved job ───────────────
    public String detectCategory(String title) {
        if (title == null) return "Other";
        String t = title.toLowerCase();

        if (anyOf(t, "java","python","developer","engineer","software","backend",
                "frontend","fullstack","full stack","react","angular","vue",
                "spring","node","devops","cloud","aws","azure","gcp","data",
                "ml","ai","machine learning","android","ios","mobile","sre",
                "platform","infrastructure","security","cyber","blockchain",
                "golang","rust","kotlin","scala","php","ruby","typescript","c++",
                ".net","sap","salesforce","tableau","power bi","spark","kafka",
                "generative","llm","databricks","snowflake"))
            return "IT";

        if (anyOf(t, "hr","human resource","recruiter","talent","people ops",
                "hrbp","payroll","compensation"))
            return "HR";

        if (anyOf(t, "sales","business development","account executive",
                "revenue","pre-sales","inside sales","enterprise sales"))
            return "Sales";

        if (anyOf(t, "finance","accounting","ca","cpa","audit","tax",
                "treasury","controller","financial analyst"))
            return "Finance";

        if (anyOf(t, "marketing","seo","content","brand","growth",
                "digital marketing","social media","ppc"))
            return "Marketing";

        if (anyOf(t, "product manager","product owner","product lead"))
            return "Product";

        if (anyOf(t, "design","ui","ux","graphic","visual","figma"))
            return "Design";

        if (anyOf(t, "operations","supply chain","logistics","procurement"))
            return "Operations";

        return "Other";
    }

    // ── Called by JobService to resolve skills for each saved job ─────────────
    public List<String> extractSkills(String text) {
        if (text == null) return List.of("General IT");
        String t = text.toLowerCase();
        List<String> skills = new ArrayList<>();
        buildSkillMap().forEach((k, v) -> { if (t.contains(k)) skills.add(v); });
        if (skills.isEmpty()) skills.add("General IT");
        return skills;
    }

    // ── Public static so scrapers (Base*Scraper) can reuse it ────────────────
    public static Map<String, String> buildSkillMap() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("java",             "Java");
        m.put("python",           "Python");
        m.put("react",            "React");
        m.put("angular",          "Angular");
        m.put("vue",              "Vue.js");
        m.put("spring",           "Spring Boot");
        m.put("node",             "Node.js");
        m.put("express",          "Express.js");
        m.put("django",           "Django");
        m.put("flask",            "Flask");
        m.put("fastapi",          "FastAPI");
        m.put("sql",              "SQL");
        m.put("postgresql",       "PostgreSQL");
        m.put("mysql",            "MySQL");
        m.put("mongodb",          "MongoDB");
        m.put("redis",            "Redis");
        m.put("elasticsearch",    "Elasticsearch");
        m.put("aws",              "AWS");
        m.put("azure",            "Azure");
        m.put("gcp",              "GCP");
        m.put("devops",           "DevOps");
        m.put("docker",           "Docker");
        m.put("kubernetes",       "Kubernetes");
        m.put("terraform",        "Terraform");
        m.put("ansible",          "Ansible");
        m.put("jenkins",          "Jenkins");
        m.put("ci/cd",            "CI/CD");
        m.put("git",              "Git");
        m.put(".net",             ".NET");
        m.put("c#",               "C#");
        m.put("c++",              "C++");
        m.put("rust",             "Rust");
        m.put("golang",           "Go");
        m.put("kotlin",           "Kotlin");
        m.put("swift",            "Swift");
        m.put("flutter",          "Flutter");
        m.put("android",          "Android");
        m.put("ios",              "iOS");
        m.put("react native",     "React Native");
        m.put("typescript",       "TypeScript");
        m.put("javascript",       "JavaScript");
        m.put("php",              "PHP");
        m.put("ruby",             "Ruby");
        m.put("scala",            "Scala");
        m.put("sap",              "SAP");
        m.put("salesforce",       "Salesforce");
        m.put("tableau",          "Tableau");
        m.put("power bi",         "Power BI");
        m.put("machine learning", "Machine Learning");
        m.put("deep learning",    "Deep Learning");
        m.put("data science",     "Data Science");
        m.put("nlp",              "NLP");
        m.put("llm",              "LLM");
        m.put("generative ai",    "Generative AI");
        m.put("spark",            "Apache Spark");
        m.put("kafka",            "Apache Kafka");
        m.put("hadoop",           "Hadoop");
        m.put("airflow",          "Airflow");
        m.put("dbt",              "dbt");
        m.put("snowflake",        "Snowflake");
        m.put("databricks",       "Databricks");
        m.put("microservice",     "Microservices");
        m.put("graphql",          "GraphQL");
        m.put("grpc",             "gRPC");
        m.put("security",         "Security");
        m.put("blockchain",       "Blockchain");
        return m;
    }

    private boolean anyOf(String text, String... keywords) {
        for (String k : keywords) if (text.contains(k)) return true;
        return false;
    }
}