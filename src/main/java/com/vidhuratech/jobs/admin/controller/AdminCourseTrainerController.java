package com.vidhuratech.jobs.admin.controller;

import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.common.notification.service.ActivityNotificationService;
import com.vidhuratech.jobs.common.security.SecurityUtils;
import com.vidhuratech.jobs.lms.course.entity.Course;
import com.vidhuratech.jobs.lms.course.entity.CourseTrainerAssignment;
import com.vidhuratech.jobs.lms.course.repository.CourseRepository;
import com.vidhuratech.jobs.lms.course.repository.CourseTrainerAssignmentRepository;
import com.vidhuratech.jobs.user.entity.User;
import com.vidhuratech.jobs.user.enums.UserRole;
import com.vidhuratech.jobs.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin/course-trainers")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
@RequiredArgsConstructor
public class AdminCourseTrainerController {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseTrainerAssignmentRepository assignmentRepository;
    private final SecurityUtils securityUtils;
    private final ActivityNotificationService notificationService;

    @GetMapping("/trainers")
    public ApiResponse<?> trainers() {
        List<Map<String, Object>> trainers = userRepository
                .findByRoleAndDeletedFalseAndActiveTrue(UserRole.TRAINER)
                .stream()
                .map(this::mapTrainer)
                .toList();

        return ApiResponse.success(trainers);
    }

    @GetMapping("/assignments")
    public ApiResponse<?> assignments() {
        List<Map<String, Object>> data = assignmentRepository.findActiveDetailed()
                .stream()
                .map(this::mapAssignment)
                .toList();

        return ApiResponse.success(data);
    }

    @PostMapping("/assign")
    @Transactional
    public ApiResponse<?> assign(@RequestBody Map<String, Object> payload) {
        Long courseId = Long.valueOf(String.valueOf(payload.get("courseId")));
        Long trainerId = Long.valueOf(String.valueOf(payload.get("trainerId")));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        User trainer = userRepository
                .findByIdAndRoleAndDeletedFalseAndActiveTrue(trainerId, UserRole.TRAINER)
                .orElseThrow(() -> new RuntimeException("Active trainer not found"));

        Optional<CourseTrainerAssignment> existing =
                assignmentRepository.findByCourseIdAndTrainerIdAndActiveTrue(courseId, trainerId);

        if (existing.isPresent()) {
            CourseTrainerAssignment existingAssignment = existing.get();

            notificationService.notifyTrainer(
                    trainer,
                    "Course assignment already active",
                    "You are already assigned to course: " + course.getTitle(),
                    "TRAINER_COURSE_ALREADY_ASSIGNED",
                    "/dashboard/trainer/courses"
            );

            return ApiResponse.success(
                    mapAssignmentWithDetails(existingAssignment, course, trainer),
                    "Trainer already assigned to this course"
            );
        }

        CourseTrainerAssignment assignment = CourseTrainerAssignment.builder()
                .courseId(course.getId())
                .trainerId(trainer.getId())
                .active(true)
                .assignedAt(LocalDateTime.now())
                .assignedByUserId(securityUtils.getCurrentUserId())
                .build();

        CourseTrainerAssignment saved = assignmentRepository.save(assignment);

        notificationService.notifyTrainer(
                trainer,
                "Course assigned",
                "Admin assigned you to course: " + course.getTitle(),
                "TRAINER_COURSE_ASSIGNED",
                "/dashboard/trainer/courses"
        );

        notificationService.notifyAdmins(
                "Trainer assigned to course",
                trainer.getName() + " assigned to " + course.getTitle(),
                "TRAINER_COURSE_ASSIGNED",
                "/dashboard/admin/manage-trainers"
        );

        return ApiResponse.success(
                mapAssignmentWithDetails(saved, course, trainer),
                "Trainer assigned successfully"
        );
    }

    @PutMapping("/assignments/{id}/deactivate")
    @Transactional
    public ApiResponse<?> deactivate(@PathVariable Long id) {
        CourseTrainerAssignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        Course course = courseRepository.findById(assignment.getCourseId())
                .orElse(null);

        User trainer = userRepository.findById(assignment.getTrainerId())
                .orElse(null);

        assignment.setActive(false);
        assignmentRepository.save(assignment);

        if (trainer != null) {
            notificationService.notifyTrainer(
                    trainer,
                    "Course assignment removed",
                    "Your course assignment was removed: " +
                            (course == null ? "Course" : course.getTitle()),
                    "TRAINER_COURSE_UNASSIGNED",
                    "/dashboard/trainer/courses"
            );
        }

        notificationService.notifyAdmins(
                "Trainer assignment removed",
                (trainer == null ? "Trainer" : trainer.getName()) +
                        " removed from " +
                        (course == null ? "course" : course.getTitle()),
                "TRAINER_COURSE_UNASSIGNED",
                "/dashboard/admin/manage-trainers"
        );

        return ApiResponse.success(null, "Trainer assignment deactivated");
    }

    private Map<String, Object> mapTrainer(User trainer) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", trainer.getId());
        map.put("name", trainer.getName());
        map.put("email", trainer.getEmail());
        map.put("phone", trainer.getPhone());
        map.put("active", Boolean.TRUE.equals(trainer.getActive()));
        return map;
    }

    private Map<String, Object> mapAssignment(CourseTrainerAssignment assignment) {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("id", assignment.getId());
        map.put("courseId", assignment.getCourseId());
        map.put(
                "courseTitle",
                assignment.getCourse() == null ? "" : assignment.getCourse().getTitle()
        );
        map.put(
                "courseCode",
                assignment.getCourse() == null ? "" : assignment.getCourse().getCode()
        );
        map.put("trainerId", assignment.getTrainerId());
        map.put(
                "trainerName",
                assignment.getTrainer() == null ? "" : assignment.getTrainer().getName()
        );
        map.put(
                "trainerEmail",
                assignment.getTrainer() == null ? "" : assignment.getTrainer().getEmail()
        );
        map.put("active", Boolean.TRUE.equals(assignment.getActive()));
        map.put("assignedAt", assignment.getAssignedAt());

        return map;
    }

    private Map<String, Object> mapAssignmentWithDetails(
            CourseTrainerAssignment assignment,
            Course course,
            User trainer
    ) {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("id", assignment.getId());
        map.put("courseId", assignment.getCourseId());
        map.put("courseTitle", course == null ? "" : course.getTitle());
        map.put("courseCode", course == null ? "" : course.getCode());
        map.put("trainerId", assignment.getTrainerId());
        map.put("trainerName", trainer == null ? "" : trainer.getName());
        map.put("trainerEmail", trainer == null ? "" : trainer.getEmail());
        map.put("active", Boolean.TRUE.equals(assignment.getActive()));
        map.put("assignedAt", assignment.getAssignedAt());

        return map;
    }
}