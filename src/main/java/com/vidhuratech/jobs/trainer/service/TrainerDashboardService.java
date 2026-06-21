package com.vidhuratech.jobs.trainer.service;

import com.vidhuratech.jobs.common.notification.service.ActivityNotificationService;
import com.vidhuratech.jobs.dashboard.dto.DashboardStatsResponse;
import com.vidhuratech.jobs.lms.batch.entity.Batch;
import com.vidhuratech.jobs.lms.batch.repository.BatchEnrollmentRepository;
import com.vidhuratech.jobs.lms.batch.repository.BatchRepository;

import com.vidhuratech.jobs.common.security.SecurityUtils;
import com.vidhuratech.jobs.lms.course.entity.Course;
import com.vidhuratech.jobs.lms.course.entity.CourseTrainerAssignment;
import com.vidhuratech.jobs.lms.course.repository.CourseRepository;
import com.vidhuratech.jobs.lms.course.repository.CourseTrainerAssignmentRepository;
import com.vidhuratech.jobs.trainer.dto.TrainingContentDTO;
import com.vidhuratech.jobs.trainer.entity.*;
import com.vidhuratech.jobs.trainer.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TrainerDashboardService {

    private final BatchRepository batchRepository;
    private final BatchEnrollmentRepository enrollmentRepository;
    private final CurriculumRepository curriculumRepository;
    private final SecurityUtils securityUtils;
    private final ObjectMapper objectMapper;
    private final TrainerWorkflowService workflowService;
    private final ActivityNotificationService notificationService;
    private final TrainingSubmissionRepository submissionRepository;
    private final MockInterviewRequestRepository mockRepository;
    private final TrainingContentRepository contentRepository;
    private final CourseTrainerAssignmentRepository courseTrainerAssignmentRepository;
    private final CourseRepository courseRepository;
    private final TrainerContentStorageService trainerContentStorageService;

    public DashboardStatsResponse getDashboard() {
        String email = securityUtils.getCurrentUserEmail();

        long assignedBatches = batchRepository.countByTrainerEmail(email);
        long totalStudents = enrollmentRepository.countStudentsByTrainerEmail(email);
        long pendingReviews = submissionRepository.countByTrainerEmailAndStatus(email, TrainingSubmissionStatus.SUBMITTED);
        long mockRequests = mockRepository.countByTrainerEmailAndStatus(email, MockInterviewStatus.REQUESTED);

        List<Map<String, Object>> batches = getBatches();

        long requestedMocks = mockRepository.countByTrainerEmailAndStatus(email, MockInterviewStatus.REQUESTED);
        long scheduledMocks = mockRepository.countByTrainerEmailAndStatus(email, MockInterviewStatus.SCHEDULED);
        long completedMocks = mockRepository.countByTrainerEmailAndStatus(email, MockInterviewStatus.COMPLETED);
        List<Map<String, Object>> courses = getAssignedCourses();

        Map<String, Object> stats = new HashMap<>();
        stats.put("assignedBatches", assignedBatches);
        stats.put("totalStudents", totalStudents);
        stats.put("pendingReviews", pendingReviews);
        stats.put("todaysSessions", scheduledMocks);
        stats.put("avgAttendance", 0);
        stats.put("assignmentsSubmitted", submissionRepository.findByTrainerEmail(email).size());
        stats.put("requestedMocks", requestedMocks);
        stats.put("scheduledMocks", scheduledMocks);
        stats.put("completedMocks", completedMocks);
        stats.put("practiceItems", contentRepository.countByTrainerEmailAndType(email, TrainingContentType.PRACTICE));
        stats.put("materials", contentRepository.countByTrainerEmailAndType(email, TrainingContentType.MATERIAL));
        stats.put("notes", contentRepository.countByTrainerEmailAndType(email, TrainingContentType.NOTE));
        stats.put("contentUploaded", contentRepository.countByTrainerEmail(email));
        stats.put("assignedCourses", courses.size());
        Map<String, List<?>> sections = new HashMap<>();
        sections.put("batches", batches);
        sections.put("upcomingSessions", workflowService.getMockInterviewRequests().stream().limit(5).toList());
        sections.put("studentActivities", workflowService.getSubmissions().stream().limit(5).toList());
        sections.put("courses", courses);
        return DashboardStatsResponse.builder()
                .stats(stats)
                .sections(sections)
                .build();
    }

    public List<Map<String, Object>> getBatches() {
        String email = securityUtils.getCurrentUserEmail();

        return batchRepository.findByTrainerEmail(email)
                .stream()
                .map(batch -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", batch.getId());
                    map.put("name", batch.getName());
                    map.put("course", batch.getCourse() != null ? batch.getCourse().getTitle() : "Course");
                    map.put("students", enrollmentRepository.countByBatchId(batch.getId()));
                    map.put("status", batch.getStatus());
                    map.put("zoomTime", batch.getZoomTime());
                    return map;
                })
                .toList();
    }

    public List<?> getStudents() {
        return workflowService.getStudents();
    }

    public void saveOrUpdateCurriculum(Long batchId, String json) {
        try {
            objectMapper.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON format");
        }

        String email = securityUtils.getCurrentUserEmail();

        batchRepository.findByIdAndTrainerEmail(batchId, email)
                .orElseThrow(() -> new RuntimeException("This batch is not assigned to your trainer account"));

        Optional<Curriculum> existing = curriculumRepository.findByBatchId(batchId);

        Curriculum curriculum = existing.orElseGet(() -> Curriculum.builder()
                .batchId(batchId)
                .trainerEmail(email)
                .build());

        curriculum.setJsonData(json);
        curriculumRepository.save(curriculum);

        Batch batch = batchRepository.findById(batchId).orElse(null);
        if (batch != null) {
            notificationService.notifyBatchStudents(
                    batch.getEnrollments(),
                    "Curriculum updated",
                    "Curriculum updated for " + batch.getName(),
                    "CURRICULUM_UPDATED",
                    "/dashboard/student/lms"
            );
        }
    }

    public Optional<Curriculum> getCurriculum(Long batchId) {
        String email = securityUtils.getCurrentUserEmail();

        batchRepository.findByIdAndTrainerEmail(batchId, email)
                .orElseThrow(() -> new RuntimeException("This batch is not assigned to your trainer account"));

        return curriculumRepository.findByBatchId(batchId);
    }

    public String getCurriculumPreview(Long batchId) {
        return curriculumRepository.findByBatchId(batchId)
                .map(Curriculum::getJsonData)
                .orElse(null);
    }

    public TrainingContent uploadContent(
            Long batchId,
            TrainingContentType type,
            String title,
            String description,
            MultipartFile file,
            String jsonData,
            String links
    ) {
        String email = securityUtils.getCurrentUserEmail();

        batchRepository.findByIdAndTrainerEmail(batchId, email)
                .orElseThrow(() -> new RuntimeException("Access denied"));

        if (jsonData != null && !jsonData.isBlank()) {
            try {
                objectMapper.readTree(jsonData);
            } catch (Exception e) {
                throw new RuntimeException("Invalid JSON format");
            }
        }

        if (links != null && !links.isBlank()) {
            try {
                objectMapper.readTree(links);
            } catch (Exception e) {
                throw new RuntimeException("Invalid links format");
            }
        }

        try {
            String fileUrl = null;
            if (file != null && !file.isEmpty()) {
                fileUrl = trainerContentStorageService.store(file);
            }

            TrainingContent content = TrainingContent.builder()
                    .batchId(batchId)
                    .trainerEmail(email)
                    .type(type)
                    .title(title)
                    .description(description)
                    .jsonData(jsonData != null && !jsonData.isBlank() ? jsonData : null)
                    .links(links != null && !links.isBlank() ? links : null)
                    .fileName(file != null && !file.isEmpty() ? file.getOriginalFilename() : null)
                    .fileType(file != null && !file.isEmpty() ? file.getContentType() : null)
                    .fileData(null)
                    .fileUrl(fileUrl)
                    .createdAt(LocalDateTime.now())
                    .build();

            TrainingContent saved = contentRepository.save(content);

            Batch batch = batchRepository.findById(batchId).orElse(null);
            if (batch != null) {
                notificationService.notifyBatchStudents(
                        batch.getEnrollments(),
                        "New learning content",
                        "Trainer uploaded " + saved.getType() + ": " + saved.getTitle(),
                        "CONTENT_UPLOADED",
                        "/dashboard/student/lms"
                );

                notificationService.notifyAdmins(
                        "Trainer uploaded content",
                        saved.getTitle() + " uploaded for batch " + batch.getName(),
                        "CONTENT_UPLOADED",
                        "/dashboard/admin/batches"
                );
            }
            return saved;
        } catch (Exception e) {
            throw new RuntimeException("Unable to upload content");
        }
    }

    public List<TrainingContentDTO> getTrainerContent() {
        String email = securityUtils.getCurrentUserEmail();

        return contentRepository.findByTrainerEmailOrderByCreatedAtDesc(email)
                .stream()
                .map(TrainingContentDTO::from)
                .toList();
    }

    public TrainingContent getContentFile(Long id) {
        return contentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Content not found"));
    }

    public List<Map<String, Object>> getAssignedCourses() {
        String email = securityUtils.getCurrentUserEmail();

        return courseTrainerAssignmentRepository.findActiveDetailedByTrainerEmail(email)
                .stream()
                .map(this::mapAssignedCourse)
                .toList();
    }

    private Map<String, Object> mapAssignedCourse(CourseTrainerAssignment assignment) {
        Course course = assignment.getCourse();

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("assignmentId", assignment.getId());
        map.put("courseId", assignment.getCourseId());
        map.put("title", course == null ? "Course" : course.getTitle());
        map.put("code", course == null ? "" : course.getCode());
        map.put("description", course == null ? "" : course.getDescription());
        map.put("level", course == null ? "" : course.getLevel());
        map.put("durationHours", course == null ? 0 : course.getDurationHours());
        map.put("price", course == null ? 0 : course.getPrice());
        map.put("status", course == null ? "" : course.getStatus());
        map.put("thumbnailUrl", course == null ? "" : course.getThumbnailUrl());
        map.put("assignedAt", assignment.getAssignedAt());
        map.put("autoMonthlyBatchEnabled", course != null && Boolean.TRUE.equals(course.getAutoMonthlyBatchEnabled()));

        return map;
    }

    // ==========================================================================
    // COURSE-LEVEL CURRICULUM WORKFLOW (Trainer Drafting & Admin Approval)
    // ==========================================================================

    private String generateDefaultCurriculumJson(Course course) {
        String title = course.getTitle().toLowerCase();
        List<Map<String, Object>> list = new ArrayList<>();
        if (title.contains("java")) {
            list.add(createModule("Week 1: Java Foundations", Arrays.asList("Introduction to Java & JDK installation", "Variables, Data Types & basic operators", "Conditional statements (if-else, switch)", "String manipulation & parsing methods")));
            list.add(createModule("Week 2: Control Flow & Loops", Arrays.asList("Looping mechanisms (while, for, do-while)", "Loop controls (break, continue)", "Function definitions & parameter scopes", "Method overloading & recursive functions")));
            list.add(createModule("Week 3: Object-Oriented Java", Arrays.asList("OOP principles & classes", "Encapsulation & Constructors", "Inheritance models & polymorphic methods", "Interfaces & Abstract classes")));
            list.add(createModule("Week 4: Exception Handling & File IO", Arrays.asList("Checked & Unchecked exceptions", "Try-catch-finally block syntax", "Custom Exception declarations", "File streams (FileInputStream, BufferedReader)")));
            list.add(createModule("Week 5: Collections Framework", Arrays.asList("List interface & Array vs LinkedList", "Set interface & hashing definitions", "Map interface & key-value pairings", "Iterator & Collections utility methods")));
            list.add(createModule("Week 6: Advanced Streams & Lambda", Arrays.asList("Lambda expressions & functional interfaces", "Stream API operations (filter, map, collect)", "Optional class & null safety", "Concurrency basics & thread pools")));
            list.add(createModule("Week 7: Capstone Project Build", Arrays.asList("Maven build configuration", "Spring Boot starter setup", "REST API mapping controllers", "Production deployment prep")));
        } else if (title.contains("python")) {
            list.add(createModule("Week 1: Python Basics", Arrays.asList("Introduction to Python & pip installation", "Variables, Operators & basic data types", "Conditional branches (if-elif-else)", "Basic user inputs & format outputs")));
            list.add(createModule("Week 2: Control Flow & Lists", Arrays.asList("Looping scripts (while, for range)", "List creation, indexings & slices", "List comprehension syntaxes", "Tuple basics & read-only storage")));
            list.add(createModule("Week 3: Dictionaries & Sets", Arrays.asList("Dictionary mappings & key lookups", "Set operations (union, intersection)", "Function declarations & arguments", "Lambda, map & filter operations")));
            list.add(createModule("Week 4: File Operations & Errors", Arrays.asList("File reading & writing operations", "Exception handling (try-except-finally)", "JSON parsing and saving data", "Standard library modules (os, sys, math)")));
            list.add(createModule("Week 5: Object-Oriented Python", Arrays.asList("OOP structures & classes", "Constructors & self mappings", "Inheritance & method overrides", "Magic/Dunder methods")));
            list.add(createModule("Week 6: Advanced Python & Packages", Arrays.asList("Decorators & closure scopes", "Generators & yield statements", "Virtual environment controls", "Package management & distribution")));
        } else if (title.contains("react")) {
            list.add(createModule("Week 1: React Core Concepts", Arrays.asList("Modern JavaScript ES6 revision", "React setup & create-react-app/Vite", "JSX rendering patterns", "Components & props properties")));
            list.add(createModule("Week 2: React State & Hooks", Arrays.asList("State management (useState)", "Component lifecycles (useEffect)", "Handling form inputs & selections", "Styling React interfaces")));
            list.add(createModule("Week 3: Advanced React Hooks", Arrays.asList("Context API & global themes", "Ref references (useRef)", "Memoization hooks (useMemo, useCallback)", "Custom hooks formulation")));
            list.add(createModule("Week 4: State Management (Redux)", Arrays.asList("Redux Core principles", "Redux Toolkit Slices & Actions", "Store configurations & Provider mapping", "Async thunks & side effects")));
            list.add(createModule("Week 5: Routing & API Integrations", Arrays.asList("React Router configuration", "Protected routes & auth gates", "Axios HTTP request handling", "Production builds & hosting")));
        } else if (title.contains("devops") || title.contains("cloud")) {
            list.add(createModule("Week 1: Cloud & Linux Foundations", Arrays.asList("Basic UNIX commands & shells", "AWS Cloud concepts & EC2 servers", "Security Groups & IAM permissions", "SSH & server remote access")));
            list.add(createModule("Week 2: Containerization (Docker)", Arrays.asList("Docker container concepts", "Dockerfile writing & configurations", "Docker volume mappings & ports", "Docker Compose multi-service pipelines")));
            list.add(createModule("Week 3: Orchestration (Kubernetes)", Arrays.asList("Kubernetes core concepts", "Deployment & Service YAMLs", "ConfigMaps & Secrets properties", "Kubernetes cluster administration")));
            list.add(createModule("Week 4: Continuous Integrations (CI/CD)", Arrays.asList("CI/CD pipeline principles", "GitHub Actions workflows", "Auto-testing & build automations", "Docker Hub remote image pushing")));
            list.add(createModule("Week 5: Infrastructure as Code (Terraform)", Arrays.asList("IaC concepts & providers", "Terraform resources & states", "Variables & output mappings", "Multi-stage environment provisioning")));
            list.add(createModule("Week 6: Monitoring & Logging", Arrays.asList("Prometheus metric tracking", "Grafana dashboards setup", "Centralized logging systems", "Incident alert pipelines")));
        } else {
            list.add(createModule("Week 1: Core Concepts & Setup", Arrays.asList("Installation, environment configuration", "Variables, Operators & syntaxes", "Basic input/output & script executions", "Conditional logic & code branching")));
            list.add(createModule("Week 2: Control Flow & Structures", Arrays.asList("Loop controls & scopes", "Arrays/Lists collections", "Custom functions & return types", "Scope parameters & local variables")));
            list.add(createModule("Week 3: Intermediate Logic & Modularization", Arrays.asList("Error Handling & validations", "Object-Oriented definitions", "Libraries & package imports", "Connecting external database servers")));
            list.add(createModule("Week 4: API & Dynamic Integrations", Arrays.asList("RESTful request frameworks", "HTTP methods & controllers", "Database operations", "Testing logic workflows")));
            list.add(createModule("Week 5: Advanced Review & Capstone Projects", Arrays.asList("Comprehensive code reviews", "Troubleshooting runtime warnings", "Writing deployment documents", "Syllabus revision & final evaluations")));
        }
        
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("id", course.getId().toString());
        root.put("name", course.getTitle());
        root.put("curriculum", list);
        
        try {
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            return "{\"curriculum\":[]}";
        }
    }

    private Map<String, Object> createModule(String title, List<String> topics) {
        Map<String, Object> module = new LinkedHashMap<>();
        module.put("title", title);
        module.put("topics", topics);
        return module;
    }

    @Cacheable(value = "curriculums", key = "#courseId")
    public Curriculum getOrCreateCourseCurriculum(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
                
        // Fetch published curriculum first
        Optional<Curriculum> existing = curriculumRepository.findByCourseIdAndPublished(courseId, true);
        if (existing.isPresent()) {
            return existing.get();
        }
        
        // Fallback: If not found, check if a draft or any curriculum exists for this course
        Optional<Curriculum> anyExisting = curriculumRepository.findByCourseId(courseId);
        if (anyExisting.isPresent()) {
            Curriculum curr = anyExisting.get();
            curr.setPublished(true);
            return curriculumRepository.save(curr);
        }
        
        // Seeding: Generate a new default curriculum, save as published, and return
        String json = generateDefaultCurriculumJson(course);
        Curriculum newCurr = Curriculum.builder()
                .courseId(courseId)
                .published(true)
                .trainerEmail("system")
                .jsonData(json)
                .build();
        return curriculumRepository.save(newCurr);
     }

     @CacheEvict(value = "curriculums", key = "#courseId")
     public Curriculum saveCourseCurriculumDraft(Long courseId, String json) {
         try {
             objectMapper.readTree(json);
         } catch (Exception e) {
             throw new RuntimeException("Invalid JSON format");
         }
         
         String email = securityUtils.getCurrentUserEmail();
         
         Optional<Curriculum> existing = curriculumRepository.findByCourseIdAndTrainerEmail(courseId, email)
                 .stream().filter(c -> Boolean.FALSE.equals(c.getPublished())).findFirst();
                 
         Curriculum curriculum = existing.orElseGet(() -> Curriculum.builder()
                 .courseId(courseId)
                 .trainerEmail(email)
                 .published(false)
                 .build());
                 
         curriculum.setJsonData(json);
         return curriculumRepository.save(curriculum);
     }

     public List<Curriculum> getPendingCurriculums() {
         return curriculumRepository.findByPublishedFalse();
     }

     @CacheEvict(value = "curriculums", key = "#result.courseId", condition = "#result != null && #result.courseId != null")
     public Curriculum publishCurriculum(Long id) {
         Curriculum draft = curriculumRepository.findById(id)
                 .orElseThrow(() -> new RuntimeException("Curriculum not found"));
                 
         Long courseId = draft.getCourseId();
         if (courseId != null) {
             curriculumRepository.findByCourseIdAndPublished(courseId, true)
                     .ifPresent(existing -> {
                         existing.setPublished(false);
                         curriculumRepository.save(existing);
                     });
         }
         
         draft.setPublished(true);
         return curriculumRepository.save(draft);
     }
}
