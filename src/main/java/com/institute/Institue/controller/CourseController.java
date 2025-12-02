package com.institute.Institue.controller;

import com.institute.Institue.dto.CourseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @GetMapping
    public ResponseEntity<List<CourseDto>> list() {
        return ResponseEntity.ok(Collections.emptyList());
    }
}

