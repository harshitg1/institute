package com.institute.Institue.bootstrap;

import com.institute.Institue.model.*;
import com.institute.Institue.model.enums.AttendanceStatus;
import com.institute.Institue.model.enums.PaymentProvider;
import com.institute.Institue.model.enums.PaymentStatus;
import com.institute.Institue.model.enums.StudentStatus;
import com.institute.Institue.model.enums.UserRole;
import com.institute.Institue.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@Profile("!prod")
@RequiredArgsConstructor
public class DatabaseSeeder implements ApplicationRunner {

    private static final String SUPER_ADMIN_EMAIL = "superadmin@institute.local";
    private static final UUID SUPER_ADMIN_ROLE_ID = UUID.fromString("10101010-1010-1010-1010-101010101010");
    private static final UUID ORG_ADMIN_ROLE_ID = UUID.fromString("20202020-2020-2020-2020-202020202020");
    private static final UUID TUTOR_ROLE_ID = UUID.fromString("30303030-3030-3030-3030-303030303030");
    private static final UUID STUDENT_ROLE_ID = UUID.fromString("40404040-4040-4040-4040-404040404040");
    private static final UUID SUPER_ADMIN_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ORG_ADMIN_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID TUTOR_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID STUDENT1_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID STUDENT2_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID ORG_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
    private static final UUID COURSE1_ID = UUID.fromString("77777777-7777-7777-7777-777777777777");
    private static final UUID COURSE2_ID = UUID.fromString("88888888-8888-8888-8888-888888888888");
    private static final UUID LESSON1_ID = UUID.fromString("90909090-9090-9090-9090-909090909090");
    private static final UUID LESSON2_ID = UUID.fromString("91919191-9191-9191-9191-919191919191");
    private static final UUID LESSON3_ID = UUID.fromString("92929292-9292-9292-9292-929292929292");
    private static final UUID BATCH_A_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID BATCH_B_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    private static final UUID BATCH_STUDENT_1_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    private static final UUID BATCH_STUDENT_2_ID = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
    private static final UUID BATCH_STUDENT_3_ID = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");
    private static final UUID PAYMENT_ORDER_1_ID = UUID.fromString("dddddddd-0000-0000-0000-000000000001");
    private static final UUID PAYMENT_ORDER_2_ID = UUID.fromString("dddddddd-0000-0000-0000-000000000002");
    private static final UUID ENROLLMENT_1_ID = UUID.fromString("eeeeeeee-0000-0000-0000-000000000001");
    private static final UUID ENROLLMENT_2_ID = UUID.fromString("eeeeeeee-0000-0000-0000-000000000002");
    private static final UUID REFRESH_TOKEN_1_ID = UUID.fromString("ffffffff-0000-0000-0000-000000000001");
    private static final UUID REFRESH_TOKEN_2_ID = UUID.fromString("ffffffff-0000-0000-0000-000000000002");
    private static final UUID OTP_ID = UUID.fromString("abababab-abab-abab-abab-abababababab");
    private static final UUID VIDEO_PROGRESS_1_ID = UUID.fromString("cdcdcdcd-cdcd-cdcd-cdcd-cdcdcdcdcdcd");
    private static final UUID VIDEO_PROGRESS_2_ID = UUID.fromString("dededede-dede-dede-dede-dededededede");
    private static final UUID TRANSFER_LOG_ID = UUID.fromString("efefefef-efef-efef-efef-efefefefefef");
    private static final UUID ATTENDANCE_1_ID = UUID.fromString("f1f1f1f1-f1f1-f1f1-f1f1-f1f1f1f1f1f1");
    private static final UUID ATTENDANCE_2_ID = UUID.fromString("f2f2f2f2-f2f2-f2f2-f2f2-f2f2f2f2f2f2");
    private static final UUID ATTENDANCE_3_ID = UUID.fromString("f3f3f3f3-f3f3-f3f3-f3f3-f3f3f3f3f3f3");
    private static final UUID ATTENDANCE_4_ID = UUID.fromString("f4f4f4f4-f4f4-f4f4-f4f4-f4f4f4f4f4f4");

    private static final String SUPER_ADMIN_HASH = "$2a$10$3bkffZ96J5.lkuaLNnfjh.DRcBs3RPu.Wa5gUhxD1Vty5UFw8eIGC";
    private static final String ORG_ADMIN_HASH = "$2a$10$36cxc5t/kF4FbXcqC7wFZulJkSSGCCryZao0ZFGwJns5lQaKZ5KOO";
    private static final String TUTOR_HASH = "$2a$10$0E.Yg0hKtAeGTcUZ/dMQAuA1k6LlkzcqPoxyumJi3zm9cBJ7gA9Vy";
    private static final String STUDENT_HASH = "$2a$10$oovsxBF52CpIYimlGXzzNeY4XqNK8scxMDIWuNTWrljG6fdYgyGda";

    private final RoleRepository roleRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final BatchRepository batchRepository;
    private final BatchStudentRepository batchStudentRepository;
    private final AttendanceRepository attendanceRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BatchTransferLogRepository batchTransferLogRepository;
    private final PasswordResetOtpRepository passwordResetOtpRepository;
    private final VideoProgressRepository videoProgressRepository;
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (hasAnyExistingApplicationData()) {
            log.info("Database already contains application data, skipping bootstrap");
            return;
        }

        log.info("Seeding initial institute data");

        Role superAdminRole = upsertRole(SUPER_ADMIN_ROLE_ID, UserRole.SUPER_ADMIN);
        Role orgAdminRole = upsertRole(ORG_ADMIN_ROLE_ID, UserRole.ORG_ADMIN);
        Role tutorRole = upsertRole(TUTOR_ROLE_ID, UserRole.TUTOR);
        Role studentRole = upsertRole(STUDENT_ROLE_ID, UserRole.STUDENT);

        Organization organization = organizationRepository.save(Organization.builder()
                .id(ORG_ID)
                .name("NorthStar Institute")
                .active(true)
                .createdBy(SUPER_ADMIN_ID)
                .build());

        User superAdmin = userRepository.save(User.builder()
                .id(SUPER_ADMIN_ID)
                .email(SUPER_ADMIN_EMAIL)
                .password(SUPER_ADMIN_HASH)
                .firstName("System")
                .lastName("Super Admin")
                .role(superAdminRole)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build());

        User orgAdmin = userRepository.save(User.builder()
                .id(ORG_ADMIN_ID)
                .email("admin@northstar.local")
                .password(ORG_ADMIN_HASH)
                .firstName("Nisha")
                .lastName("Sharma")
                .organization(organization)
                .role(orgAdminRole)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build());

        User tutor = userRepository.save(User.builder()
                .id(TUTOR_ID)
                .email("tutor@northstar.local")
                .password(TUTOR_HASH)
                .firstName("Arjun")
                .lastName("Mehta")
                .organization(organization)
                .role(tutorRole)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build());

        User student1 = userRepository.save(User.builder()
                .id(STUDENT1_ID)
                .email("student1@northstar.local")
                .password(STUDENT_HASH)
                .firstName("Rahul")
                .lastName("Verma")
                .organization(organization)
                .role(studentRole)
                .studentStatus(StudentStatus.ACTIVE)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build());

        User student2 = userRepository.save(User.builder()
                .id(STUDENT2_ID)
                .email("student2@northstar.local")
                .password(STUDENT_HASH)
                .firstName("Priya")
                .lastName("Singh")
                .organization(organization)
                .role(studentRole)
                .studentStatus(StudentStatus.ACTIVE)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build());

        Course course1 = courseRepository.save(Course.builder()
                .id(COURSE1_ID)
                .title("Foundations of Mathematics")
                .description("Core math course covering algebra, linear equations, and practice sets.")
                .price(new BigDecimal("24999.00"))
                .thumbnailUrl("https://cdn.example.com/courses/foundations-of-mathematics.png")
                .durationHours(120)
                .published(true)
                .organization(organization)
                .build());

        Course course2 = courseRepository.save(Course.builder()
                .id(COURSE2_ID)
                .title("Communication Skills Lab")
                .description("Public speaking and interview preparation for students.")
                .price(new BigDecimal("18999.00"))
                .thumbnailUrl("https://cdn.example.com/courses/communication-skills-lab.png")
                .durationHours(60)
                .published(false)
                .organization(organization)
                .build());

        Lesson lesson1 = lessonRepository.save(Lesson.builder()
                .id(LESSON1_ID)
                .title("Introduction to Algebra")
                .organizationId(ORG_ID)
                .build());

        Lesson lesson2 = lessonRepository.save(Lesson.builder()
                .id(LESSON2_ID)
                .title("Linear Equations Basics")
                .organizationId(ORG_ID)
                .build());

        Lesson lesson3 = lessonRepository.save(Lesson.builder()
                .id(LESSON3_ID)
                .title("Interview Communication Practice")
                .organizationId(ORG_ID)
                .build());

        Batch batchA = batchRepository.save(Batch.builder()
                .id(BATCH_A_ID)
                .name("Mathematics Batch A")
                .duration("3 months")
                .startTime("09:00")
                .endTime("11:00")
                .instructor(tutor)
                .organization(organization)
                .active(true)
                .build());

        Batch batchB = batchRepository.save(Batch.builder()
                .id(BATCH_B_ID)
                .name("Communication Batch B")
                .duration("6 weeks")
                .startTime("12:00")
                .endTime("13:30")
                .instructor(tutor)
                .organization(organization)
                .active(true)
                .build());

        batchStudentRepository.save(BatchStudent.builder()
                .id(BATCH_STUDENT_1_ID)
                .batch(batchA)
                .student(student1)
                .active(true)
                .build());

        batchStudentRepository.save(BatchStudent.builder()
                .id(BATCH_STUDENT_2_ID)
                .batch(batchA)
                .student(student2)
                .active(false)
                .leftAt(Instant.parse("2026-04-04T12:00:00Z"))
                .build());

        batchStudentRepository.save(BatchStudent.builder()
                .id(BATCH_STUDENT_3_ID)
                .batch(batchB)
                .student(student2)
                .active(true)
                .build());

        attendanceRepository.save(Attendance.builder()
                .id(ATTENDANCE_1_ID)
                .batch(batchA)
                .student(student1)
                .date(LocalDate.of(2026, 4, 1))
                .status(AttendanceStatus.PRESENT)
                .remarks("On time and fully active in class.")
                .markedBy(tutor)
                .updatedAt(Instant.parse("2026-04-04T10:00:00Z"))
                .build());

        attendanceRepository.save(Attendance.builder()
                .id(ATTENDANCE_2_ID)
                .batch(batchA)
                .student(student2)
                .date(LocalDate.of(2026, 4, 1))
                .status(AttendanceStatus.LATE)
                .remarks("Joined after the attendance roll call.")
                .markedBy(tutor)
                .updatedAt(Instant.parse("2026-04-04T10:01:00Z"))
                .build());

        attendanceRepository.save(Attendance.builder()
                .id(ATTENDANCE_3_ID)
                .batch(batchA)
                .student(student1)
                .date(LocalDate.of(2026, 4, 2))
                .status(AttendanceStatus.EXCUSED)
                .remarks("Medical leave approved by the admin.")
                .markedBy(orgAdmin)
                .updatedAt(Instant.parse("2026-04-04T10:02:00Z"))
                .build());

        attendanceRepository.save(Attendance.builder()
                .id(ATTENDANCE_4_ID)
                .batch(batchB)
                .student(student2)
                .date(LocalDate.of(2026, 4, 3))
                .status(AttendanceStatus.ABSENT)
                .remarks("Did not attend the communication lab.")
                .markedBy(orgAdmin)
                .updatedAt(Instant.parse("2026-04-04T10:03:00Z"))
                .build());

        PaymentOrder paymentOrder1 = paymentOrderRepository.save(PaymentOrder.builder()
                .id(PAYMENT_ORDER_1_ID)
                .student(student1)
                .course(course1)
                .organization(organization)
                .amount(new BigDecimal("24999.00"))
                .currency("INR")
                .provider(PaymentProvider.RAZORPAY)
                .providerOrderId("razorpay_order_seed_001")
                .providerPaymentId("razorpay_payment_seed_001")
                .providerSignature("razorpay_signature_seed_001")
                .status(PaymentStatus.CAPTURED)
                .paymentLink("https://payments.example.com/orders/razorpay_order_seed_001")
                .createdAt(Instant.parse("2026-04-04T10:30:00Z"))
                .updatedAt(Instant.parse("2026-04-04T10:30:00Z"))
                .build());

        paymentOrderRepository.save(PaymentOrder.builder()
                .id(PAYMENT_ORDER_2_ID)
                .student(student2)
                .course(course2)
                .organization(organization)
                .amount(new BigDecimal("18999.00"))
                .currency("INR")
                .provider(PaymentProvider.STRIPE)
                .providerOrderId("stripe_order_seed_002")
                .status(PaymentStatus.FAILED)
                .failureReason("Card declined in sandbox")
                .createdAt(Instant.parse("2026-04-04T10:31:00Z"))
                .updatedAt(Instant.parse("2026-04-04T10:31:00Z"))
                .build());

        enrollmentRepository.save(Enrollment.builder()
                .id(ENROLLMENT_1_ID)
                .user(student1)
                .course(course1)
                .organization(organization)
                .purchased(true)
                .paymentOrderId(paymentOrder1.getId())
                .build());

        enrollmentRepository.save(Enrollment.builder()
                .id(ENROLLMENT_2_ID)
                .user(student2)
                .course(course2)
                .organization(organization)
                .purchased(false)
                .build());

        refreshTokenRepository.save(RefreshToken.builder()
                .id(REFRESH_TOKEN_1_ID)
                .token("seed-refresh-token-superadmin")
                .userId(superAdmin.getId())
                .build());

        refreshTokenRepository.save(RefreshToken.builder()
                .id(REFRESH_TOKEN_2_ID)
                .token("seed-refresh-token-orgadmin")
                .userId(orgAdmin.getId())
                .build());

        passwordResetOtpRepository.save(PasswordResetOtp.builder()
                .id(OTP_ID)
                .otp("482911")
                .userId(student2.getId())
                .build());

        videoProgressRepository.save(VideoProgress.builder()
                .id(VIDEO_PROGRESS_1_ID)
                .userId(student1.getId())
                .lessonId(lesson1.getId())
                .secondsWatched(900)
                .build());

        videoProgressRepository.save(VideoProgress.builder()
                .id(VIDEO_PROGRESS_2_ID)
                .userId(student2.getId())
                .lessonId(lesson3.getId())
                .secondsWatched(300)
                .build());

        batchTransferLogRepository.save(BatchTransferLog.builder()
                .id(TRANSFER_LOG_ID)
                .student(student2)
                .fromBatch(batchA)
                .toBatch(batchB)
                .reason("Promoted after the initial assessment.")
                .transferredBy(orgAdmin)
                .transferredAt(Instant.parse("2026-04-04T12:00:00Z"))
                .build());

        log.info("Seed data inserted successfully");
    }

    private boolean hasAnyExistingApplicationData() {
        return roleRepository.count() > 0
                || organizationRepository.count() > 0
                || userRepository.count() > 0
                || courseRepository.count() > 0
                || lessonRepository.count() > 0
                || batchRepository.count() > 0
                || batchStudentRepository.count() > 0
                || attendanceRepository.count() > 0
                || enrollmentRepository.count() > 0
                || paymentOrderRepository.count() > 0
                || refreshTokenRepository.count() > 0
                || batchTransferLogRepository.count() > 0
                || passwordResetOtpRepository.count() > 0
                || videoProgressRepository.count() > 0;
    }

    private Role upsertRole(UUID id, UserRole roleName) {
        return roleRepository.findByRole(roleName)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .id(id)
                        .role(roleName)
                        .build()));
    }
}
