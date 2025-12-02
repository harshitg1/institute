package com.institute.Institue.service.impl;

import com.institute.Institue.dto.CourseDto;
import com.institute.Institue.service.CourseService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class CourseServiceImpl implements CourseService {

    @Override
    public List<CourseDto> listAll() {
        return Collections.emptyList();
    }
}

