package com.polyglot.persistence.children.data;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Builder
public record ChildDataStagingRes(
        UUID id,
        String employeeId,
        String childId,
        Map<String, Object> editedFields,
        String approvalStatus,
        String requestType,
        LocalDateTime timestamp
) {
}
