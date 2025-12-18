package com.institute.Institue.controller;

import com.institute.Institue.model.Organization;
import com.institute.Institue.model.User;
import com.institute.Institue.model.Course;
import com.institute.Institue.model.Lesson;
import com.institute.Institue.model.Enrollment;
import com.institute.Institue.model.VideoProgress;
import com.institute.Institue.repository.OrganizationRepository;
import com.institute.Institue.repository.UserRepository;
import com.institute.Institue.repository.CourseRepository;
import com.institute.Institue.repository.LessonRepository;
import com.institute.Institue.repository.EnrollmentRepository;
import com.institute.Institue.repository.VideoProgressRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/superadmin")
public class SeedController {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final VideoProgressRepository videoProgressRepository;

    public SeedController(OrganizationRepository organizationRepository,
                          UserRepository userRepository,
                          CourseRepository courseRepository,
                          LessonRepository lessonRepository,
                          EnrollmentRepository enrollmentRepository,
                          VideoProgressRepository videoProgressRepository) {
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.videoProgressRepository = videoProgressRepository;
    }

    @GetMapping("/seed")
    public ResponseEntity<Map<String, Object>> seedAll() {
        Map<String, Object> result = new HashMap<>();

        // Idempotent: if super exists, return existing info
        Optional<User> maybeSuper = userRepository.findByUsername("super");
        if (maybeSuper.isPresent()) {
            result.put("status", "ALREADY_SEEDED");
            result.put("super", maybeSuper.get().getUsername());
            return ResponseEntity.ok(result);
        }

        // Create organization (or reuse if exists)
        String orgName = "Acme Academy";
        Organization org = organizationRepository.findByName(orgName).orElseGet(() -> {
            Organization o = Organization.builder()
                    .id(UUID.randomUUID())
                    .name(orgName)
                    .build();
            return organizationRepository.save(o);
        });

        // Super admin (global)
        User superUser = User.builder()
                .id(UUID.randomUUID())
                .username("super")
                .email("super@local")
                .organizationId(null)
                .roles("SUPER_ADMIN")
                .build();
        userRepository.save(superUser);

        // Org admin
        User orgAdmin = User.builder()
                .id(UUID.randomUUID())
                .username("acme_admin")
                .email("admin@acme.academy")
                .organizationId(org.getId())
                .roles("ORG_ADMIN")
                .build();
        userRepository.save(orgAdmin);

        // Tutor
        User tutor = User.builder()
                .id(UUID.randomUUID())
                .username("tutor1")
                .email("tutor1@acme.academy")
                .organizationId(org.getId())
                .roles("TUTOR")
                .build();
        userRepository.save(tutor);

        // Student
        User student = User.builder()
                .id(UUID.randomUUID())
                .username("student1")
                .email("student1@acme.academy")
                .organizationId(org.getId())
                .roles("STUDENT")
                .build();
        userRepository.save(student);

        // Course
        Course course = Course.builder()
                .id(UUID.randomUUID())
                .title("Intro to Algebra")
                .organizationId(org.getId())
                .build();
        courseRepository.save(course);

        // Lessons
        Lesson lesson1 = Lesson.builder()
                .id(UUID.randomUUID())
                .title("Lesson 1 - Basics")
                .organizationId(org.getId())
                .build();
        lessonRepository.save(lesson1);

        Lesson lesson2 = Lesson.builder()
                .id(UUID.randomUUID())
                .title("Lesson 2 - Equations")
                .organizationId(org.getId())
                .build();
        lessonRepository.save(lesson2);

        // Enrollment
        Enrollment enrollment = Enrollment.builder()
                .id(UUID.randomUUID())
                .userId(student.getId())
                .courseId(course.getId())
                .organizationId(org.getId())
                .build();
        enrollmentRepository.save(enrollment);

        // Video progress
        VideoProgress vp = VideoProgress.builder()
                .id(UUID.randomUUID())
                .userId(student.getId())
                .lessonId(lesson1.getId())
                .secondsWatched(42)
                .build();
        videoProgressRepository.save(vp);

        // Return summary
        result.put("status", "SEEDED");
        result.put("organization", Map.of("id", org.getId().toString(), "name", org.getName()));
        result.put("super", Map.of("username", superUser.getUsername(), "id", superUser.getId().toString()));
        result.put("org_admin", Map.of("username", orgAdmin.getUsername(), "id", orgAdmin.getId().toString()));
        result.put("tutor", Map.of("username", tutor.getUsername(), "id", tutor.getId().toString()));
        result.put("student", Map.of("username", student.getUsername(), "id", student.getId().toString()));
        result.put("course", Map.of("title", course.getTitle(), "id", course.getId().toString()));
        result.put("lessons", new String[]{lesson1.getId().toString(), lesson2.getId().toString()});
        result.put("enrollment", enrollment.getId().toString());
        result.put("video_progress", vp.getId().toString());

        return ResponseEntity.ok(result);
    }
}

