package com.institute.Institue.controller;

import com.institute.Institue.model.*;
import com.institute.Institue.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/superadmin")
@RequiredArgsConstructor // Replaces the manual constructor
public class SeedController {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final VideoProgressRepository videoProgressRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private Role ensureRole(String name) {
        return roleRepository.findByName(name).orElseGet(() -> {
            Role r = Role.builder().name(name).build();
            return roleRepository.save(r);
        });
    }

    @GetMapping("/seed")
    @Transactional
    public ResponseEntity<Map<String, Object>> seedAll() {
        Map<String, Object> result = new HashMap<>();
        String defaultPassword = "ChangeMe123!";

        // Ensure roles exist in the DB
        Role rSuper = ensureRole("SUPER_ADMIN");
        Role rOrgAdmin = ensureRole("ORG_ADMIN");
        Role rTutor = ensureRole("TUTOR");
        Role rStudent = ensureRole("STUDENT");

        // 1. Check if Super Admin already exists
        Optional<User> maybeSuper = userRepository.findByEmail("super@local");
        if (maybeSuper.isPresent()) {
            User superUser = maybeSuper.get();
            // Update role to ensure it's correct
            superUser.setRole(rSuper);
            userRepository.save(superUser);

            result.put("status", "ALREADY_SEEDED");
            result.put("super", superUser.getEmail());
            return ResponseEntity.ok(result);
        }

        // 2. Create Default Organization
        String orgName = "Acme Academy";
        Organization org = organizationRepository.findByName(orgName).orElseGet(() -> {
            Organization o = Organization.builder()
                    .name(orgName)
                    .build();
            return organizationRepository.save(o);
        });

        // 3. Seed Users (Assigning single Role via .role())

        // Super admin (No Org)
        User superUser = User.builder()
                .email("super@local")
                .organization(null)
                .password(passwordEncoder.encode(defaultPassword))
                .role(rSuper) // SINGLE ROLE
                .enabled(true)
                .build();
        userRepository.save(superUser);

        // Org admin
        User orgAdmin = User.builder()
                .email("admin@acme.academy")
                .organization(org)
                .password(passwordEncoder.encode(defaultPassword))
                .role(rOrgAdmin) // SINGLE ROLE
                .enabled(true)
                .build();
        userRepository.save(orgAdmin);

        // Tutor
        User tutor = User.builder()
                .email("tutor1@acme.academy")
                .organization(org)
                .password(passwordEncoder.encode(defaultPassword))
                .role(rTutor) // SINGLE ROLE
                .enabled(true)
                .build();
        userRepository.save(tutor);

        // Student
        User student = User.builder()
                .email("student1@acme.academy")
                .organization(org)
                .password(passwordEncoder.encode(defaultPassword))
                .role(rStudent) // SINGLE ROLE
                .enabled(true)
                .build();
        userRepository.save(student);

        // 4. Seed Course & Lessons
        Course course = Course.builder()
                .title("Intro to Algebra")
                .organizationId(org.getId())
                .build();
        courseRepository.save(course);

        Lesson lesson1 = Lesson.builder()
                .title("Lesson 1 - Basics")
                .organizationId(org.getId())
                .build();
        lessonRepository.save(lesson1);

        // 5. Seed Enrollment & Progress
        Enrollment enrollment = Enrollment.builder()
                .userId(student.getId())
                .courseId(course.getId())
                .organizationId(org.getId())
                .build();
        enrollmentRepository.save(enrollment);

        VideoProgress vp = VideoProgress.builder()
                .userId(student.getId())
                .lessonId(lesson1.getId())
                .secondsWatched(42)
                .build();
        videoProgressRepository.save(vp);

        result.put("status", "SEEDED");
        result.put("organization", org.getName());
        result.put("default_password", defaultPassword);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/reset-seed")
    @Transactional
    public ResponseEntity<Map<String, Object>> resetAndSeed() {
        // Delete in order to satisfy Foreign Key constraints
        videoProgressRepository.deleteAll();
        enrollmentRepository.deleteAll();
        lessonRepository.deleteAll();
        courseRepository.deleteAll();

        // Deleting users will remove the records from the 'users' table
        // and clean up the role_id references.
        userRepository.deleteAll();

        // Re-run seed logic
        return seedAll();
    }
}