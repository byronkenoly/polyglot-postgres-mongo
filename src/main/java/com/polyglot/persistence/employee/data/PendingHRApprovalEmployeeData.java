package com.polyglot.persistence.employee.data;

import java.time.LocalDateTime;
import java.util.Map;

public record PendingHRApprovalEmployeeData(
        String employeeId,
        Map<String, Object> oldValues,
        Map<String, Object>newValues,
        String approvalStatus,
        LocalDateTime timestamp
) {
}
