package com.institute.Institue.controller;

import com.institute.Institue.dto.*;
import com.institute.Institue.model.User;
import com.institute.Institue.service.BatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/batches")
@RequiredArgsConstructor
@Slf4j
public class BatchController {

    private final BatchService batchService;
    private final com.institute.Institue.repository.UserRepository userRepository;

    // Helper similar to AdminStudentController to resolve admin principal
    private Optional<User> resolveAdmin(User admin) {
        if (admin != null) return Optional.of(admin);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return Optional.empty();
        Object p = auth.getPrincipal();
        if (p == null) return Optional.empty();
        if (p instanceof User) return Optional.of((User) p);
        if (p instanceof UserDetails) {
            String username = ((UserDetails) p).getUsername();
            return userRepository.findByEmail(username);
        }
        String username = p instanceof String ? (String) p : p.toString();
        if (username == null || username.isBlank()) return Optional.empty();
        return userRepository.findByEmail(username);
    }

    /**
     * Create a new batch
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BatchResponse>> createBatch(
            @AuthenticationPrincipal User admin,
            @Valid @RequestBody BatchRequest request) {
        Optional<User> maybeAdmin = resolveAdmin(admin);
        if (maybeAdmin.isEmpty()) {
            log.warn("Create batch request without authenticated admin");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized", "UNAUTHORIZED"));
        }
        User resolved = maybeAdmin.get();
        log.info("Create batch request received for orgId={} by user={}", resolved.getOrganizationId(), resolved.getEmail());
        log.debug("Create batch payload: {}", request);
        UUID orgId = resolved.getOrganizationId();
        BatchResponse response = batchService.createBatch(orgId, request);
        log.info("Batch created id={} for orgId={}", response.getId(), orgId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * List all batches in the organization
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BatchResponse>>> listBatches(
            @AuthenticationPrincipal User admin) {
        Optional<User> maybeAdmin = resolveAdmin(admin);
        if (maybeAdmin.isEmpty()) {
            log.warn("List batches request without authenticated admin");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized", "UNAUTHORIZED"));
        }
        User resolved = maybeAdmin.get();
        log.info("List batches request for orgId={} by user={}", resolved.getOrganizationId(), resolved.getEmail());
        UUID orgId = resolved.getOrganizationId();

        List<BatchResponse> batches = batchService.listBatches(orgId);
        log.info("Returning {} batches for orgId={}", batches != null ? batches.size() : 0, orgId);
        return ResponseEntity.ok(ApiResponse.success(batches));
    }

    /**
     * Get batch details with student count
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BatchResponse>> getBatch(@PathVariable UUID id) {
        log.info("Get batch request for id={}", id);
        BatchResponse response = batchService.getBatch(id);
        log.debug("Get batch response for id={}: {}", id, response);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Update batch details
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BatchResponse>> updateBatch(
            @PathVariable UUID id,
            @Valid @RequestBody BatchRequest request) {
        log.info("Update batch request for id={}", id);
        log.debug("Update payload for id={}: {}", id, request);
        BatchResponse response = batchService.updateBatch(id, request);
        log.info("Batch updated id={}", id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Delete batch (only if no active students)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBatch(@PathVariable UUID id) {
        log.info("Delete batch request for id={}", id);
        batchService.deleteBatch(id);
        log.info("Batch deleted id={}", id);
        return ResponseEntity.ok(ApiResponse.success(null, "Batch deleted successfully"));
    }

    /**
     * List students in a batch
     */
    @GetMapping("/{id}/students")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> getStudentsInBatch(@PathVariable UUID id) {
        log.info("Get students in batch request for id={}", id);
        List<StudentResponse> students = batchService.getStudentsInBatch(id);
        log.info("Returning {} students for batch id={}", students != null ? students.size() : 0, id);
        return ResponseEntity.ok(ApiResponse.success(students));
    }
}
