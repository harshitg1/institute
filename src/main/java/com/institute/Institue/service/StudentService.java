package com.institute.Institue.service;

import com.institute.Institue.dto.*;

import java.util.List;
import java.util.UUID;

public interface StudentService {

    StudentResponse createStudent(UUID orgId, CreateStudentRequest request);

    List<StudentResponse> listStudents(UUID orgId);

    StudentResponse getStudent(UUID studentId);

    StudentResponse updateStudent(UUID studentId, CreateStudentRequest request);

    void deactivateStudent(UUID studentId);

    StudentResponse updateStudentStatus(UUID studentId, String status);

    BatchTransferResponse transferBatch(UUID studentId, BatchTransferRequest request, UUID transferredByUserId);

    StudentResponse assignCourses(UUID studentId, List<String> courseIds, UUID orgId);

    void removeCourseFromStudent(UUID studentId, UUID courseId);
}
