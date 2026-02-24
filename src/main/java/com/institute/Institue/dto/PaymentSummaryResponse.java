package com.institute.Institue.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSummaryResponse {
    private BigDecimal totalRevenue;
    private long totalTransactions;
    private long successfulTransactions;
    private long failedTransactions;
    private List<MonthlyRevenue> revenueByMonth;
    private List<CourseRevenue> revenueByCourse;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthlyRevenue {
        private String month;
        private BigDecimal revenue;
        private long count;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CourseRevenue {
        private String courseId;
        private String courseTitle;
        private BigDecimal revenue;
        private long enrollments;
    }
}
