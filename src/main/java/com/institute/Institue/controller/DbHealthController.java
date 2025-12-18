package com.institute.Institue.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class DbHealthController {

    private final DataSource dataSource;

    public DbHealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/db")
    public ResponseEntity<Map<String, Object>> checkDb() {
        try (Connection conn = dataSource.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT 1")) {
            if (rs.next()) {
                return ResponseEntity.ok(Map.of("status", "UP", "result", rs.getInt(1)));
            } else {
                return ResponseEntity.status(500).body(Map.of("status", "DOWN", "error", "no result"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("status", "DOWN", "error", e.getMessage()));
        }
    }
}

