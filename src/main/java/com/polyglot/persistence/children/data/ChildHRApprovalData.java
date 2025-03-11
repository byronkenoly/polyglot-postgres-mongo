package com.polyglot.persistence.children.data;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public abstract class ChildHRApprovalData {
    private UUID id;
    private String employeeId;
    private String approvalStatus;
    private String requestType;
    private LocalDateTime timestamp;
}
