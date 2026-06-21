package com.vidhuratech.jobs.jobs.service;

import com.vidhuratech.jobs.jobs.entity.Company;
import com.vidhuratech.jobs.jobs.entity.Job;
import com.vidhuratech.jobs.jobs.entity.Skill;
import com.vidhuratech.jobs.jobs.repository.CompanyRepository;
import com.vidhuratech.jobs.jobs.repository.JobRepository;
import com.vidhuratech.jobs.jobs.repository.SkillRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class JobSeederService {

    private final JobRepository jobRepo;
    private final CompanyRepository companyRepo;
    private final SkillRepository skillRepo;
    private final JobEnrichmentService enrichmentService;

    @PersistenceContext
    private EntityManager entityManager;

    public JobSeederService(JobRepository jobRepo,
                            CompanyRepository companyRepo,
                            SkillRepository skillRepo,
                            JobEnrichmentService enrichmentService) {
        this.jobRepo = jobRepo;
        this.companyRepo = companyRepo;
        this.skillRepo = skillRepo;
        this.enrichmentService = enrichmentService;
    }

    private static class CompanyInfo {
        String name;
        String website;
        String careerUrl;

        CompanyInfo(String name, String website, String careerUrl) {
            this.name = name;
            this.website = website;
            this.careerUrl = careerUrl;
        }
    }

    private static final List<CompanyInfo> COMPANIES_LIST = List.of(
        new CompanyInfo("Google", "https://www.google.com", "https://careers.google.com/jobs/results"),
        new CompanyInfo("Microsoft", "https://www.microsoft.com", "https://careers.microsoft.com/us/en/search-results"),
        new CompanyInfo("Amazon", "https://www.amazon.com", "https://www.amazon.jobs/en/jobs"),
        new CompanyInfo("Zoho", "https://www.zoho.com", "https://www.zoho.com/careers/jobs"),
        new CompanyInfo("Freshworks", "https://www.freshworks.com", "https://www.freshworks.com/company/careers"),
        new CompanyInfo("Flipkart", "https://www.flipkart.com", "https://www.flipkartcareers.com"),
        new CompanyInfo("Swiggy", "https://www.swiggy.com", "https://careers.swiggy.com"),
        new CompanyInfo("Zomato", "https://www.zomato.com", "https://www.zomato.com/careers"),
        new CompanyInfo("PhonePe", "https://www.phonepe.com", "https://www.phonepe.com/careers"),
        new CompanyInfo("Paytm", "https://www.paytm.com", "https://careers.paytm.com"),
        new CompanyInfo("Razorpay", "https://www.razorpay.com", "https://razorpay.com/jobs"),
        new CompanyInfo("CRED", "https://www.cred.club", "https://careers.cred.club"),
        new CompanyInfo("Meesho", "https://www.meesho.com", "https://meesho.careers"),
        new CompanyInfo("Groww", "https://www.groww.in", "https://groww.in/careers"),
        new CompanyInfo("Postman", "https://www.postman.com", "https://www.postman.com/careers"),
        new CompanyInfo("Juspay", "https://www.juspay.in", "https://juspay.in/careers"),
        new CompanyInfo("InMobi", "https://www.inmobi.com", "https://www.inmobi.com/company/careers"),
        new CompanyInfo("TCS", "https://www.tcs.com", "https://www.tcs.com/careers"),
        new CompanyInfo("Infosys", "https://www.infosys.com", "https://www.infosys.com/careers.html"),
        new CompanyInfo("Wipro", "https://www.wipro.com", "https://careers.wipro.com"),
        new CompanyInfo("Cognizant", "https://www.cognizant.com", "https://careers.cognizant.com"),
        new CompanyInfo("HCLTech", "https://www.hcltech.com", "https://www.hcltech.com/careers"),
        new CompanyInfo("Tech Mahindra", "https://www.techmahindra.com", "https://careers.techmahindra.com"),
        new CompanyInfo("LTIMindtree", "https://www.ltimindtree.com", "https://www.ltimindtree.com/careers"),
        new CompanyInfo("Capgemini", "https://www.capgemini.com", "https://www.capgemini.com/careers"),
        new CompanyInfo("Accenture", "https://www.accenture.com", "https://www.accenture.com/in-en/careers"),
        new CompanyInfo("Adobe", "https://www.adobe.com", "https://adobe.wd5.myworkdayjobs.com/external_experienced"),
        new CompanyInfo("Salesforce", "https://www.salesforce.com", "https://www.salesforce.com/company/careers"),
        new CompanyInfo("Oracle", "https://www.oracle.com", "https://eeho.fa.us2.oraclecloud.com/hcmUI/CandidateExperience/en/sites/CX_1"),
        new CompanyInfo("Cisco", "https://www.cisco.com", "https://jobs.cisco.com/jobs/SearchJobs"),
        new CompanyInfo("Intel", "https://www.intel.com", "https://jobs.intel.com"),
        new CompanyInfo("NVIDIA", "https://www.nvidia.com", "https://nvidia.wd5.myworkdayjobs.com/NVIDIAExternalCareerSite"),
        new CompanyInfo("Qualcomm", "https://www.qualcomm.com", "https://qualcomm.wd5.myworkdayjobs.com/External"),
        new CompanyInfo("Uber", "https://www.uber.com", "https://www.uber.com/careers/list"),
        new CompanyInfo("Atlassian", "https://www.atlassian.com", "https://www.atlassian.com/company/careers"),
        new CompanyInfo("Coinbase", "https://www.coinbase.com", "https://www.coinbase.com/careers"),
        new CompanyInfo("Goldman Sachs", "https://www.goldmansachs.com", "https://www.goldmansachs.com/careers"),
        new CompanyInfo("JPMorgan Chase", "https://www.jpmorganchase.com", "https://careers.jpmorgan.com/US/en/home"),
        new CompanyInfo("Morgan Stanley", "https://www.morganstanley.com", "https://www.morganstanley.com/about-us/careers"),
        new CompanyInfo("Barclays", "https://www.barclays.com", "https://search.jobs.barclays"),
        new CompanyInfo("Ola", "https://www.ola.in", "https://ola.in/careers"),
        new CompanyInfo("Blinkit", "https://www.blinkit.com", "https://blinkit.com/careers"),
        new CompanyInfo("BigBasket", "https://www.bigbasket.com", "https://careers.bigbasket.com"),
        new CompanyInfo("Nykaa", "https://www.nykaa.com", "https://careers.nykaa.com"),
        new CompanyInfo("Pocket FM", "https://www.pocketfm.com", "https://pocketfm.com/careers"),
        new CompanyInfo("Zeta", "https://www.zeta.tech", "https://www.zeta.tech/careers"),
        new CompanyInfo("TATA 1mg", "https://www.1mg.com", "https://www.1mg.com/jobs"),
        new CompanyInfo("Apollo 24|7", "https://www.apollo247.com", "https://careers.apollo247.com"),
        new CompanyInfo("UpGrad", "https://www.upgrad.com", "https://www.upgrad.com/careers"),
        new CompanyInfo("Unacademy", "https://www.unacademy.com", "https://unacademy.com/careers"),
        new CompanyInfo("PhysicsWallah", "https://www.pw.live", "https://www.pw.live/careers"),
        new CompanyInfo("Scaler", "https://www.scaler.com", "https://www.scaler.com/careers"),
        new CompanyInfo("GeeksforGeeks", "https://www.geeksforgeeks.org", "https://www.geeksforgeeks.org/careers"),
        // Additional Indian Brands
        new CompanyInfo("Lenskart", "https://www.lenskart.com", "https://lenskart.com/careers"),
        new CompanyInfo("Ola Electric", "https://www.olaelectric.com", "https://careers.olaelectric.com"),
        new CompanyInfo("Ather Energy", "https://www.atherenergy.com", "https://atherenergy.com/careers"),
        new CompanyInfo("Urban Company", "https://www.urbancompany.com", "https://urbancompany.com/careers"),
        new CompanyInfo("ShareChat", "https://www.sharechat.com", "https://careers.sharechat.com"),
        new CompanyInfo("Dailyhunt", "https://www.dailyhunt.in", "https://dailyhunt.in/careers"),
        new CompanyInfo("Zepto", "https://www.zepto.com", "https://zepto.careers"),
        new CompanyInfo("Uni Cards", "https://www.uni.club", "https://uni.club/careers"),
        new CompanyInfo("Fi Money", "https://www.fi.money", "https://fi.money/careers"),
        new CompanyInfo("Jupiter", "https://www.jupiter.money", "https://jupiter.money/careers"),
        new CompanyInfo("Slice", "https://www.sliceit.com", "https://sliceit.com/careers"),
        new CompanyInfo("CoinDCX", "https://www.coindcx.com", "https://coindcx.com/careers"),
        new CompanyInfo("WazirX", "https://www.wazirx.com", "https://wazirx.com/careers"),
        new CompanyInfo("BharatPe", "https://www.bharatpe.com", "https://bharatpe.com/careers"),
        new CompanyInfo("OneCard", "https://www.getonecard.app", "https://getonecard.app/careers"),
        new CompanyInfo("Mobile Premier League", "https://www.mpl.live", "https://mpl.live/careers"),
        new CompanyInfo("Dream11", "https://www.dream11.com", "https://dream11.com/careers"),
        new CompanyInfo("Games24x7", "https://www.games24x7.com", "https://games24x7.com/careers"),
        new CompanyInfo("Purplle", "https://www.purplle.com", "https://purplle.com/careers"),
        new CompanyInfo("Mamaearth", "https://www.mamaearth.in", "https://careers.mamaearth.in"),
        new CompanyInfo("CarDekho", "https://www.cardekho.com", "https://cardekho.com/careers"),
        new CompanyInfo("Spinny", "https://www.spinny.com", "https://spinny.com/careers"),
        new CompanyInfo("Cars24", "https://www.cars24.com", "https://cars24.com/careers"),
        new CompanyInfo("Yellow.ai", "https://www.yellow.ai", "https://yellow.ai/careers"),
        new CompanyInfo("Haptik", "https://www.haptik.ai", "https://haptik.ai/careers"),
        new CompanyInfo("BrowserStack", "https://www.browserstack.com", "https://browserstack.com/careers"),
        new CompanyInfo("Chargebee", "https://www.chargebee.com", "https://chargebee.com/careers"),
        new CompanyInfo("Darwinbox", "https://www.darwinbox.com", "https://darwinbox.com/careers"),
        new CompanyInfo("Keka", "https://www.keka.com", "https://keka.com/careers"),
        new CompanyInfo("Signzy", "https://www.signzy.com", "https://signzy.com/careers")
    );

    private static class RoleTemplate {
        String title;
        String roleCategory;
        List<String> primarySkills;
        List<String> secondarySkills;
        boolean isInternship;

        RoleTemplate(String title, String roleCategory, List<String> primarySkills, List<String> secondarySkills, boolean isInternship) {
            this.title = title;
            this.roleCategory = roleCategory;
            this.primarySkills = primarySkills;
            this.secondarySkills = secondarySkills;
            this.isInternship = isInternship;
        }
    }

    private static final List<RoleTemplate> ROLE_TEMPLATES = List.of(
        // Backend
        new RoleTemplate("Backend Engineer", "Backend Developer", List.of("Java", "Spring Boot", "SQL"), List.of("Docker", "Kubernetes", "Redis", "AWS"), false),
        new RoleTemplate("Senior Backend Engineer", "Backend Developer", List.of("Java", "Spring Boot", "Microservices"), List.of("Kubernetes", "Apache Kafka", "GCP", "PostgreSQL"), false),
        new RoleTemplate("Java Developer", "Backend Developer", List.of("Java", "Spring Boot", "Hibernate"), List.of("MySQL", "Git", "Docker", "CI/CD"), false),
        new RoleTemplate("Python Engineer", "Backend Developer", List.of("Python", "Django", "FastAPI"), List.of("PostgreSQL", "Redis", "Docker", "Git"), false),
        new RoleTemplate("Node.js Developer", "Backend Developer", List.of("Node.js", "Express.js", "TypeScript"), List.of("MongoDB", "Redis", "Docker", "GraphQL"), false),
        new RoleTemplate("Go Developer", "Backend Developer", List.of("Go", "gRPC", "Microservices"), List.of("Kubernetes", "Docker", "PostgreSQL", "CI/CD"), false),
        new RoleTemplate("Software Development Engineer II (Backend)", "Backend Developer", List.of("Java", "Spring Boot", "Microservices"), List.of("Apache Kafka", "Elasticsearch", "AWS"), false),
        new RoleTemplate("Backend Engineering Intern", "Backend Developer", List.of("Java", "SQL", "Git"), List.of("Spring Boot", "MySQL", "Docker"), true),
        new RoleTemplate("Software Engineering Intern - Backend", "Backend Developer", List.of("Python", "SQL", "Git"), List.of("FastAPI", "MongoDB"), true),

        // Frontend
        new RoleTemplate("Frontend Engineer", "Frontend Developer", List.of("React", "TypeScript", "JavaScript"), List.of("Redux", "Tailwind CSS", "Git"), false),
        new RoleTemplate("Senior Frontend Developer", "Frontend Developer", List.of("React", "TypeScript", "Next.js"), List.of("GraphQL", "Cypress", "AWS", "CI/CD"), false),
        new RoleTemplate("Angular Developer", "Frontend Developer", List.of("Angular", "TypeScript", "JavaScript"), List.of("RxJS", "Sass", "Git", "Docker"), false),
        new RoleTemplate("UI Engineer", "Frontend Developer", List.of("React", "JavaScript", "CSS3"), List.of("Figma", "Tailwind CSS", "HTML5"), false),
        new RoleTemplate("Frontend Developer (Vue.js)", "Frontend Developer", List.of("Vue.js", "JavaScript", "TypeScript"), List.of("Vuex", "Tailwind CSS", "Git"), false),
        new RoleTemplate("Frontend Engineering Intern", "Frontend Developer", List.of("JavaScript", "HTML5", "CSS3"), List.of("React", "Tailwind CSS", "Git"), true),
        new RoleTemplate("React Developer Intern", "Frontend Developer", List.of("React", "JavaScript", "Git"), List.of("TypeScript", "Redux"), true),

        // Fullstack
        new RoleTemplate("Full Stack Developer", "Full Stack Developer", List.of("Java", "Spring Boot", "React"), List.of("PostgreSQL", "Docker", "AWS", "TypeScript"), false),
        new RoleTemplate("Senior Full Stack Engineer", "Full Stack Developer", List.of("Node.js", "React", "TypeScript"), List.of("MongoDB", "GraphQL", "Kubernetes", "AWS"), false),
        new RoleTemplate("MERN Stack Developer", "Full Stack Developer", List.of("Node.js", "Express.js", "React"), List.of("MongoDB", "JavaScript", "Git", "Docker"), false),
        new RoleTemplate("Full Stack Intern", "Full Stack Developer", List.of("JavaScript", "React", "Node.js"), List.of("MongoDB", "SQL", "Git"), true),

        // DevOps & Cloud
        new RoleTemplate("DevOps Engineer", "DevOps Engineer", List.of("Docker", "Kubernetes", "AWS"), List.of("Terraform", "Ansible", "CI/CD", "Linux"), false),
        new RoleTemplate("Site Reliability Engineer (SRE)", "DevOps Engineer", List.of("Linux", "AWS", "Kubernetes"), List.of("Python", "Docker", "Terraform", "Ansible"), false),
        new RoleTemplate("Cloud Infrastructure Engineer", "DevOps Engineer", List.of("AWS", "Azure", "Terraform"), List.of("Docker", "Kubernetes", "Linux", "Git"), false),
        new RoleTemplate("DevOps Engineering Intern", "DevOps Engineer", List.of("Linux", "Git", "Docker"), List.of("AWS", "Jenkins", "CI/CD"), true),

        // Mobile
        new RoleTemplate("Mobile App Developer (Android)", "IT", List.of("Kotlin", "Android", "Java"), List.of("Git", "SQLite", "CI/CD"), false),
        new RoleTemplate("iOS Developer", "IT", List.of("Swift", "iOS", "Git"), List.of("Xcode", "CI/CD", "Objective-C"), false),
        new RoleTemplate("React Native Developer", "IT", List.of("React Native", "TypeScript", "JavaScript"), List.of("React", "Git", "Android", "iOS"), false),
        new RoleTemplate("Flutter Developer", "IT", List.of("Flutter", "Kotlin", "Swift"), List.of("Git", "Firebase", "Android", "iOS"), false),
        new RoleTemplate("Mobile App Intern", "IT", List.of("Kotlin", "JavaScript", "Git"), List.of("Flutter", "React Native", "Firebase"), true),

        // Data, ML & AI
        new RoleTemplate("Data Scientist", "IT", List.of("Python", "Machine Learning", "SQL"), List.of("Deep Learning", "NLP", "TensorFlow", "PyTorch"), false),
        new RoleTemplate("Machine Learning Engineer", "IT", List.of("Python", "Deep Learning", "PyTorch"), List.of("Machine Learning", "LLM", "Docker", "AWS"), false),
        new RoleTemplate("AI Research Engineer", "IT", List.of("Python", "LLM", "Generative AI"), List.of("Deep Learning", "NLP", "PyTorch", "GCP"), false),
        new RoleTemplate("Data Engineer", "IT", List.of("Python", "SQL", "Apache Spark"), List.of("Apache Kafka", "Snowflake", "Databricks", "Airflow"), false),
        new RoleTemplate("Data Analyst", "IT", List.of("SQL", "Python", "Power BI"), List.of("Tableau", "Excel", "Apache Spark"), false),
        new RoleTemplate("Data Science Intern", "IT", List.of("Python", "SQL", "Git"), List.of("Machine Learning", "Pandas", "NumPy"), true),

        // QA & Testing
        new RoleTemplate("QA Automation Engineer", "IT", List.of("Selenium", "Java", "SQL"), List.of("JUnit", "TestNG", "Git", "CI/CD"), false),
        new RoleTemplate("SDET (Software Development Engineer in Test)", "IT", List.of("Java", "Selenium", "Playwright"), List.of("TypeScript", "Docker", "CI/CD", "API Testing"), false),
        new RoleTemplate("QA Automation Intern", "IT", List.of("Java", "Selenium", "Git"), List.of("Python", "SQL", "Playwright"), true),

        // Product Management
        new RoleTemplate("Product Manager", "Product", List.of("SQL", "Agile", "Jira"), List.of("Product Strategy", "Figma", "Data Analysis"), false),
        new RoleTemplate("Associate Product Manager", "Product", List.of("Jira", "Agile", "Figma"), List.of("SQL", "Product Metrics"), false),

        // Design
        new RoleTemplate("UI/UX Designer", "Design", List.of("Figma", "Design System"), List.of("HTML5", "CSS3", "Adobe Creative Suite"), false),
        new RoleTemplate("Product Designer", "Design", List.of("Figma", "UX Research"), List.of("Design System", "Wireframing"), false),
        new RoleTemplate("UI/UX Design Intern", "Design", List.of("Figma", "Wireframing"), List.of("CSS3", "Design System"), true),

        // Walk-In Drives
        new RoleTemplate("Walk-In Drive: Java Developer", "Backend Developer", List.of("Java", "Spring Boot", "SQL"), List.of("Hibernate", "Docker"), false),
        new RoleTemplate("Walk-In: React Front End Developer", "Frontend Developer", List.of("React", "JavaScript", "TypeScript"), List.of("Redux", "Tailwind CSS"), false),
        new RoleTemplate("Walk-In Interview: Full Stack Engineer", "Full Stack Developer", List.of("Java", "Spring Boot", "React"), List.of("PostgreSQL", "Git"), false),
        new RoleTemplate("Walk-In Drive: DevOps Engineer", "DevOps Engineer", List.of("Docker", "Kubernetes", "AWS"), List.of("CI/CD", "Linux"), false),
        new RoleTemplate("Walk-In Drive: QA Automation Tester", "IT", List.of("Selenium", "Java", "SQL"), List.of("API Testing", "Git"), false),
        new RoleTemplate("Walk-In Drive: System & Desktop Support", "IT", List.of("Linux", "SQL", "Git"), List.of("Security", "General IT"), false)
    );

    private static final List<String> INDIAN_LOCATIONS = List.of(
        "Bengaluru", "Hyderabad", "Pune", "Noida", "Gurugram",
        "Mumbai", "Chennai", "Kochi", "Coimbatore", "Kolkata"
    );

    @Transactional
    public Map<String, Object> seedJobs(int count, boolean cleanFirst) {
        long startTime = System.currentTimeMillis();
        Map<String, Object> response = new LinkedHashMap<>();

        if (cleanFirst) {
            System.out.println("🧹 CLEARING EXISTING JOB TABLES...");
            entityManager.createNativeQuery("TRUNCATE TABLE job_skills CASCADE").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM jobs").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM companies").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM skills").executeUpdate();
            entityManager.flush();
            System.out.println("✅ TABLES CLEARED SUCCESSFULLY.");
        }

        // 1. Pre-populate all skills into a Map to avoid database select trips
        System.out.println("🌱 PRE-SEEDING SKILLS CLOUD...");
        Map<String, Skill> skillsMap = new HashMap<>();
        Map<String, String> rawSkills = enrichmentService.buildSkillMap();
        for (String skillName : rawSkills.values()) {
            Skill skill = skillRepo.findByNameIgnoreCase(skillName)
                    .orElseGet(() -> skillRepo.save(new Skill(null, skillName)));
            skillsMap.put(skillName.toLowerCase(), skill);
        }
        skillRepo.flush();

        // 2. Pre-populate and save all companies into a Map
        System.out.println("🌱 PRE-SEEDING CORPORATE BRANDS...");
        Map<String, Company> companyMap = new HashMap<>();
        for (CompanyInfo compInfo : COMPANIES_LIST) {
            Company company = companyRepo.findByNameIgnoreCase(compInfo.name)
                    .orElseGet(() -> {
                        Company c = new Company();
                        c.setName(compInfo.name);
                        c.setWebsite(compInfo.website);
                        c.setLogoUrl("https://www.google.com/s2/favicons?domain=" + compInfo.name.toLowerCase().replace(" ", "") + ".com&sz=128");
                        return companyRepo.save(c);
                    });
            companyMap.put(compInfo.name.toLowerCase(), company);
        }
        companyRepo.flush();

        // 3. Generate and batch insert jobs
        System.out.println("🚀 GENERATING " + count + " HIGH-FIDELITY OPPORTUNITIES...");
        Random rand = new Random();
        List<Job> batchJobs = new ArrayList<>();
        int savedCount = 0;

        for (int i = 0; i < count; i++) {
            // Randomly select company and template
            CompanyInfo compInfo = COMPANIES_LIST.get(rand.nextInt(COMPANIES_LIST.size()));
            Company company = companyMap.get(compInfo.name.toLowerCase());

            RoleTemplate template = ROLE_TEMPLATES.get(rand.nextInt(ROLE_TEMPLATES.size()));

            Job job = new Job();
            job.setTitle(template.title);
            job.setRole(template.roleCategory);
            job.setCompany(company);
            job.setSource("Corporate Board");
            job.setCreatedAt(LocalDateTime.now());

            // Check if walkin
            boolean isWalkIn = template.title.toLowerCase().contains("walk-in") || template.title.toLowerCase().contains("walkin");

            // Randomize Remote status vs. specific Indian hub
            if (isWalkIn) {
                // Walk-ins must be on-site
                job.setRemote(false);
                job.setLocation(INDIAN_LOCATIONS.get(rand.nextInt(INDIAN_LOCATIONS.size())));
            } else {
                boolean isRemote = rand.nextBoolean();
                if (isRemote || template.title.toLowerCase().contains("remote")) {
                    job.setRemote(true);
                    job.setLocation("Remote");
                } else {
                    job.setRemote(false);
                    job.setLocation(INDIAN_LOCATIONS.get(rand.nextInt(INDIAN_LOCATIONS.size())));
                }
            }

            // Assign Experience and Job Type
            if (template.isInternship) {
                job.setJobType("Internship");
                job.setEmploymentType("Internship");
                job.setExperience("0-1 years");
                job.setMinExperience(0);
                job.setMaxExperience(1);
            } else if (isWalkIn) {
                job.setJobType("Walk-in");
                job.setEmploymentType("Walk-in");
                int expOption = rand.nextInt(2);
                if (expOption == 0) {
                    job.setExperience("0-2 years");
                    job.setMinExperience(0);
                    job.setMaxExperience(2);
                } else {
                    job.setExperience("1-4 years");
                    job.setMinExperience(1);
                    job.setMaxExperience(4);
                }
            } else {
                job.setJobType("Full-time");
                job.setEmploymentType("Full-time");
                // Randomize experience ranges
                int expOption = rand.nextInt(5);
                if (expOption == 0) { // Entry level (Fresher)
                    job.setExperience("0-2 years");
                    job.setMinExperience(0);
                    job.setMaxExperience(2);
                } else if (expOption == 1) { // Mid level
                    job.setExperience("3-5 years");
                    job.setMinExperience(3);
                    job.setMaxExperience(5);
                } else if (expOption == 2) { // Senior
                    job.setExperience("5-8 years");
                    job.setMinExperience(5);
                    job.setMaxExperience(8);
                } else if (expOption == 3) { // Lead
                    job.setExperience("8-12 years");
                    job.setMinExperience(8);
                    job.setMaxExperience(12);
                } else { // Manager/Director
                    job.setExperience("10+ years");
                    job.setMinExperience(10);
                    job.setMaxExperience(15);
                }
            }

            // Category classification
            job.setCategory(enrichmentService.detectCategory(template.title));

            // Compensation mapping
            if (template.isInternship) {
                int stipend = 15000 + rand.nextInt(35000);
                job.setSalary(String.format("₹%,d - ₹%,d / month", stipend, stipend + 15000));
            } else {
                int minExp = job.getMinExperience();
                if (minExp <= 2) { // Entry
                    int base = 400000 + rand.nextInt(400000);
                    job.setSalary(String.format("₹%,d - ₹%,d L.P.A", base, base + 300000));
                } else if (minExp <= 5) { // Mid
                    int base = 1000000 + rand.nextInt(800000);
                    job.setSalary(String.format("₹%,d - ₹%,d L.P.A", base, base + 500000));
                } else if (minExp <= 8) { // Senior
                    int base = 2000000 + rand.nextInt(1200000);
                    job.setSalary(String.format("₹%,d - ₹%,d L.P.A", base, base + 800000));
                } else { // Lead/Manager
                    int base = 3500000 + rand.nextInt(2500000);
                    job.setSalary(String.format("₹%,d - ₹%,d L.P.A", base, base + 1500000));
                }
            }

            // Generate authentic specific apply URL
            job.setApplyLink(compInfo.careerUrl);

            // Assign Skills Cloud
            Set<Skill> jobSkills = new HashSet<>();
            List<String> assignedSkillsList = new ArrayList<>();

            // 2-3 primary skills
            for (String ps : template.primarySkills) {
                Skill skill = skillsMap.get(ps.toLowerCase());
                if (skill != null) {
                    jobSkills.add(skill);
                    assignedSkillsList.add(ps);
                }
            }
            // 1-2 secondary skills
            int secondaryCount = 1 + rand.nextInt(2);
            for (int k = 0; k < secondaryCount; k++) {
                String ss = template.secondarySkills.get(rand.nextInt(template.secondarySkills.size()));
                Skill skill = skillsMap.get(ss.toLowerCase());
                if (skill != null) {
                    jobSkills.add(skill);
                    if (!assignedSkillsList.contains(ss)) {
                        assignedSkillsList.add(ss);
                    }
                }
            }
            job.setSkills(jobSkills);
            job.setSkillsCsv(String.join(",", assignedSkillsList));

            // Dynamic HTML Description
            job.setDescription(generateDescription(
                    job.getTitle(),
                    compInfo.name,
                    job.getLocation(),
                    job.getExperience(),
                    job.getSalary(),
                    assignedSkillsList,
                    isWalkIn
            ));

            // Randomize postedAt in last 30 days
            job.setPostedAt(LocalDateTime.now().minusDays(rand.nextInt(30)).minusHours(rand.nextInt(24)));

            batchJobs.add(job);

            // Save in batches of 1000 using entity manager to control memory overhead
            if (batchJobs.size() >= 1000) {
                saveBatch(batchJobs);
                savedCount += batchJobs.size();
                batchJobs.clear();
                System.out.println("💾 Seeded batch: " + savedCount + " jobs...");
            }
        }

        // Insert remaining records
        if (!batchJobs.isEmpty()) {
            saveBatch(batchJobs);
            savedCount += batchJobs.size();
            batchJobs.clear();
        }

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("🎉 SEEDING COMPLETED! Total time taken: " + duration + " ms.");

        response.put("status", "SUCCESS");
        response.put("jobsSeededCount", savedCount);
        response.put("companiesCount", companyMap.size());
        response.put("skillsCount", skillsMap.size());
        response.put("timeTakenMs", duration);

        return response;
    }

    private void saveBatch(List<Job> jobs) {
        for (Job job : jobs) {
            entityManager.persist(job);
        }
        entityManager.flush();
        entityManager.clear();
    }

    private String generateDescription(String title, String company, String location, String experience, String salary, List<String> skills, boolean isWalkIn) {
        StringBuilder sb = new StringBuilder();
        sb.append("<p><strong>About the Role:</strong><br/>");
        sb.append("We are seeking a talented and proactive <strong>").append(title).append("</strong> to join <strong>").append(company).append("</strong>'s engineering workforce. ");
        sb.append("In this position, you will work from our hub in <strong>").append(location).append("</strong> to implement, maintain, and optimize key software systems and features.</p>");

        if (isWalkIn) {
            sb.append("<p><strong>🚶 Walk-In Interview Details:</strong><br/>");
            sb.append("<strong>Interview Dates:</strong> 25th to 28th June 2026<br/>");
            sb.append("<strong>Reporting Time:</strong> 09:30 AM to 02:30 PM<br/>");
            sb.append("<strong>Venue:</strong> ").append(company).append(" India Corporate Hub, ").append(location).append(", India.<br/>");
            sb.append("<em>Note: Please carry 3 printed copies of your updated resume and a valid Government ID proof.</em></p>");
        }

        sb.append("<p><strong>Key Responsibilities:</strong><ul>");
        sb.append("<li>Architect and implement robust, clean, and efficient business solutions.</li>");
        sb.append("<li>Work closely with product managers, QA automation leads, and UX designers to deliver enterprise-scale features.</li>");
        sb.append("<li>Debug complex application workflows and enhance cloud infrastructure layers.</li>");
        sb.append("<li>Participate in constructive peer code reviews to enforce strict technical quality controls.</li>");
        sb.append("<li>Author and maintain high-fidelity unit and integration test suites.</li>");
        sb.append("</ul></p>");

        sb.append("<p><strong>Required Qualifications & Skills:</strong><ul>");
        sb.append("<li>Experience Requirement: ").append(experience).append(".</li>");
        sb.append("<li>Core Stack Proficiency: ").append(String.join(", ", skills)).append(".</li>");
        sb.append("<li>Strong familiarity with software design patterns and database normalisation concepts.</li>");
        sb.append("<li>Demonstrated ability to write performant code under tight microservice constraints.</li>");
        sb.append("<li>Strong verbal and written collaboration skills.</li>");
        sb.append("</ul></p>");

        sb.append("<p><strong>Compensation & Perks:</strong><ul>");
        sb.append("<li>Salary/Stipend: ").append(salary).append(" (dependent on experience and skills match).</li>");
        sb.append("<li>Flexible working schedules with remote options.</li>");
        sb.append("<li>Comprehensive family medical coverage plans.</li>");
        sb.append("<li>Generous learning budgets and support for cloud certification pathways.</li>");
        sb.append("</ul></p>");

        return sb.toString();
    }
}
