package com.vidhuratech.jobs.jobs.scraper;

import com.vidhuratech.jobs.jobs.entity.Job;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.*;
import java.time.*;
import java.util.*;
import java.util.function.Function;

public abstract class BaseSeleniumScraper {

    protected WebDriver createDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions opts = new ChromeOptions();
        opts.addArguments(
                "--headless=new", "--no-sandbox",
                "--disable-dev-shm-usage", "--disable-gpu",
                "--window-size=1920,1080",
                "--disable-blink-features=AutomationControlled",
                "--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0.0.0 Safari/537.36"
        );
        opts.addArguments("--disable-dev-shm-usage");
        opts.addArguments("--no-sandbox");
        opts.addArguments("--disable-gpu");
        opts.addArguments("--disable-extensions");
        opts.addArguments("--disable-features=VizDisplayCompositor");
        opts.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
        WebDriver driver = new ChromeDriver(opts);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        return driver;
    }

    // ✅ NEW — scroll to load lazy content
    protected void scrollDown(WebDriver d) {
        try {
            ((JavascriptExecutor) d).executeScript(
                    "window.scrollTo(0, document.body.scrollHeight)");
            Thread.sleep(1500);
        } catch (Exception ignored) {}
    }

    // ✅ NEW — wait + scroll combo
    protected void waitAndScroll(WebDriver d, String css, int seconds) {
        waitFor(d, css, seconds);
        scrollDown(d);
    }

    protected String text(WebElement el, String css) {
        try { return el.findElement(By.cssSelector(css)).getText().trim(); }
        catch (Exception e) { return ""; }
    }

    protected String text(WebDriver d, String css) {
        try { return d.findElement(By.cssSelector(css)).getText().trim(); }
        catch (Exception e) { return ""; }
    }

    protected String attr(WebElement el, String css, String attribute) {
        try { return el.findElement(By.cssSelector(css)).getAttribute(attribute); }
        catch (Exception e) { return ""; }
    }

    protected void waitFor(WebDriver d, String css, int seconds) {
        try {
            new WebDriverWait(d, Duration.ofSeconds(seconds))
                    .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(css)));
        } catch (Exception ignored) {}
    }

    protected Job build(String title, String company, String location,
                        String exp, String desc, String jobType,
                        List<String> skills, String link) {
        Job job = new Job();
        job.setTitle(title.isEmpty() ? "Software Engineer" : title);
        job.setRole(title);
        job.setCompanyName(company);
        job.setLocation(location.isEmpty() ? "India" : location);
        job.setExperience(exp.isEmpty() ? "0-3 years" : exp);
        job.setJobType(jobType);
        job.setCategory("IT");
        job.setEmploymentType("Full-time");
        job.setDescription(desc);
        job.setSalary("Not Disclosed");
        job.setSkillsCsv("java,react");
        job.setRemote(location.toLowerCase().contains("remote"));
        job.setApplyLink(link);
        job.setSource(company);
        job.setPostedAt(LocalDateTime.now());
        return job;
    }

    protected List<String> extractSkills(String text) {
        text = text.toLowerCase();
        List<String> s = new ArrayList<>();
        Map<String, String> map = new LinkedHashMap<>();
        map.put("java",            "Java");
        map.put("python",          "Python");
        map.put("react",           "React");
        map.put("angular",         "Angular");
        map.put("spring",          "Spring Boot");
        map.put("node",            "Node.js");
        map.put("sql",             "SQL");
        map.put("aws",             "AWS");
        map.put("azure",           "Azure");
        map.put("devops",          "DevOps");
        map.put("docker",          "Docker");
        map.put("kubernetes",      "Kubernetes");
        map.put(".net",            ".NET");
        map.put("sap",             "SAP");
        map.put("salesforce",      "Salesforce");
        map.put("tableau",         "Tableau");
        map.put("power bi",        "Power BI");
        map.put("machine learning","ML");
        map.put("data science",    "Data Science");
        map.put("typescript",      "TypeScript");
        map.put("golang",          "Go");
        map.put("kotlin",          "Kotlin");
        map.put("flutter",         "Flutter");
        map.put("android",         "Android");
        map.put("ios",             "iOS");
        map.put("swift",           "Swift");
        map.put("c++",             "C++");
        map.put("rust",            "Rust");
        map.put("spark",           "Apache Spark");
        map.put("kafka",           "Kafka");
        map.put("redis",           "Redis");
        map.put("elasticsearch",   "Elasticsearch");
        map.put("terraform",       "Terraform");
        map.put("jenkins",         "Jenkins");
        map.put("git",             "Git");
        String finalText = text;
        map.forEach((k, v) -> { if (finalText.contains(k)) s.add(v); });
        if (s.isEmpty()) s.add("General IT");
        return s;
    }

    protected List<Job> paginateAndScrape(
            WebDriver driver,
            String jobCardCss,
            Function<WebElement, Job> jobMapper,
            String nextBtnCss
    ) {
        List<Job> allJobs = new ArrayList<>();
        int page = 1;

        while (true) {
            try {
                System.out.println("📄 Scraping page: " + page);

                waitFor(driver, jobCardCss, 20);
                scrollDown(driver);

                List<WebElement> cards = driver.findElements(By.cssSelector(jobCardCss));

                if (cards.isEmpty()) {
                    System.out.println("⚠️ No jobs found on page " + page);
                    break;
                }

                for (WebElement c : cards) {
                    try {
                        Job job = jobMapper.apply(c);
                        if (job != null) allJobs.add(job);
                    } catch (Exception e) {
                        System.out.println("❌ Error parsing job card");
                    }
                }

                // 👉 Try clicking next page
                try {
                    WebElement nextBtn = driver.findElement(By.cssSelector(nextBtnCss));

                    if (!nextBtn.isDisplayed() || !nextBtn.isEnabled()) {
                        System.out.println("⛔ No more pages");
                        break;
                    }

                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", nextBtn);
                    Thread.sleep(2500);

                    page++;

                } catch (Exception e) {
                    System.out.println("⛔ Pagination ended");
                    break;
                }

            } catch (Exception e) {
                System.out.println("❌ Page error: " + e.getMessage());
                break;
            }
        }

        System.out.println("✅ Total jobs collected: " + allJobs.size());
        return allJobs;
    }

    protected List<WebElement> loadAllByScroll(WebDriver driver, String jobCss) {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        long lastHeight = (long) js.executeScript("return document.body.scrollHeight");

        while (true) {
            js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
            try { Thread.sleep(2000); } catch (Exception ignored) {}

            long newHeight = (long) js.executeScript("return document.body.scrollHeight");
            if (newHeight == lastHeight) break;
            lastHeight = newHeight;
        }

        return driver.findElements(By.cssSelector(jobCss));
    }
}