package com.institute.Institue.controller;

import com.institute.Institue.model.Organization;
import com.institute.Institue.model.User;
import com.institute.Institue.model.Course;
import com.institute.Institue.model.Lesson;
import com.institute.Institue.model.Enrollment;
import com.institute.Institue.model.VideoProgress;
import com.institute.Institue.model.Role;
import com.institute.Institue.repository.OrganizationRepository;
import com.institute.Institue.repository.UserRepository;
import com.institute.Institue.repository.CourseRepository;
import com.institute.Institue.repository.LessonRepository;
import com.institute.Institue.repository.EnrollmentRepository;
import com.institute.Institue.repository.VideoProgressRepository;
import com.institute.Institue.repository.RoleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

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
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public SeedController(OrganizationRepository organizationRepository,
                          UserRepository userRepository,
                          CourseRepository courseRepository,
                          LessonRepository lessonRepository,
                          EnrollmentRepository enrollmentRepository,
                          VideoProgressRepository videoProgressRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder) {
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.videoProgressRepository = videoProgressRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private Role ensureRole(String name) {
        return roleRepository.findByName(name).orElseGet(() -> {
            Role r = Role.builder().id(UUID.randomUUID()).name(name).build();
            return roleRepository.save(r);
        });
    }

    @GetMapping("/seed")
    public ResponseEntity<Map<String, Object>> seedAll() {
        Map<String, Object> result = new HashMap<>();

        // default dev password for seeded users (change in production)
        String defaultPassword = "ChangeMe123!";

        // Idempotent: if super exists, return existing info
        Optional<User> maybeSuper = userRepository.findByEmail("super@local");
        if (maybeSuper.isPresent()) {
            // Ensure SUPER_ADMIN role is attached even if user existed before
            Role rSuper = ensureRole("SUPER_ADMIN");
            User superUser = maybeSuper.get();
            boolean hadRole = superUser.getRoles().stream().anyMatch(r -> "SUPER_ADMIN".equalsIgnoreCase(r.getName()));
            if (!hadRole) {
                superUser.getRoles().add(rSuper);
                userRepository.save(superUser);
            }
            result.put("status", "ALREADY_SEEDED");
            result.put("super", superUser.getEmail());
            result.put("default_password", defaultPassword);
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

        // ensure roles exist
        Role rSuper = ensureRole("SUPER_ADMIN");
        Role rOrgAdmin = ensureRole("ORG_ADMIN");
        Role rTutor = ensureRole("TUTOR");
        Role rStudent = ensureRole("STUDENT");

        // Super admin (global)
        User superUser = User.builder()
                .id(UUID.randomUUID())
                .email("super@local")
                .organizationId(null)
                .password(passwordEncoder.encode(defaultPassword))
                .build();
        superUser.getRoles().add(rSuper);
        userRepository.save(superUser);

        // Org admin
        User orgAdmin = User.builder()
                .id(UUID.randomUUID())
                .email("admin@acme.academy")
                .organizationId(org.getId())
                .password(passwordEncoder.encode(defaultPassword))
                .build();
        orgAdmin.getRoles().add(rOrgAdmin);
        userRepository.save(orgAdmin);

        // Tutor
        User tutor = User.builder()
                .id(UUID.randomUUID())
                .email("tutor1@acme.academy")
                .organizationId(org.getId())
                .password(passwordEncoder.encode(defaultPassword))
                .build();
        tutor.getRoles().add(rTutor);
        userRepository.save(tutor);

        // Student
        User student = User.builder()
                .id(UUID.randomUUID())
                .email("student1@acme.academy")
                .organizationId(org.getId())
                .password(passwordEncoder.encode(defaultPassword))
                .build();
        student.getRoles().add(rStudent);
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
        result.put("super", Map.of("email", superUser.getEmail(), "id", superUser.getId().toString()));
        result.put("org_admin", Map.of("email", orgAdmin.getEmail(), "id", orgAdmin.getId().toString()));
        result.put("tutor", Map.of("email", tutor.getEmail(), "id", tutor.getId().toString()));
        result.put("student", Map.of("email", student.getEmail(), "id", student.getId().toString()));
        result.put("course", Map.of("title", course.getTitle(), "id", course.getId().toString()));
        result.put("lessons", new String[]{lesson1.getId().toString(), lesson2.getId().toString()});
        result.put("enrollment", enrollment.getId().toString());
        result.put("video_progress", vp.getId().toString());
        result.put("default_password", defaultPassword);

        return ResponseEntity.ok(result);
    }

    // New: Reset bad data and re-seed with clean defaults
    @GetMapping("/reset-seed")
    @Transactional
    public ResponseEntity<Map<String, Object>> resetAndSeed() {
        Map<String, Object> result = new HashMap<>();

        // Delete dependent records first to avoid FK issues
        videoProgressRepository.deleteAll();
        enrollmentRepository.deleteAll();
        lessonRepository.deleteAll();
        courseRepository.deleteAll();

        // Delete all users to guarantee clean reseed (user_roles will cascade)
        userRepository.deleteAll();

        // Optionally keep roles and organizations to avoid churn. Ensure base roles exist.
        ensureRole("SUPER_ADMIN");
        ensureRole("ORG_ADMIN");
        ensureRole("TUTOR");
        ensureRole("STUDENT");

        // Re-run seed
        ResponseEntity<Map<String, Object>> seeded = seedAll();
        Map<String, Object> seedBody = seeded.getBody();
        if (seedBody != null) {
            seedBody.put("reset", true);
            return ResponseEntity.ok(seedBody);
        }
        result.put("status", "RESET_DONE_SEED_FAILED");
        return ResponseEntity.status(500).body(result);
    }
}
