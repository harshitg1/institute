package com.institute.Institue.repository;

import com.institute.Institue.model.Course;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository {
    Optional<Course> findById(String id);
    List<Course> findAll();
}

