package com.polyglot.persistence.employee.domain;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "employee_data_staging")
public class EmployeeDataStaging {

    @Id
    private UUID id;

    @Field(name = "employee_id")
    private String employeeId;

    @Field(name = "edited_fields")
    private Map<String, Object> editedFields;

    @Field(name = "approval_status")
    private String approvalStatus;

    @Field(name = "date_time")
    private LocalDateTime timestamp;
}
