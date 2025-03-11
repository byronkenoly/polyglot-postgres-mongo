package com.polyglot.persistence.children.domain;

import com.polyglot.persistence.children.data.ChildDataStagingRes;
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
@Document(collection = "children_data_staging")
public class ChildDataStaging {

    @Id
    private UUID id;

    @Field(name = "employee_id")
    private String employeeId;

    @Field(name = "child_id")
    private String childId;

    @Field(name = "edited_fields")
    private Map<String, Object> editedFields;

    @Field(name = "approval_status")
    private String approvalStatus;

    @Field(name = "request_type")
    private String requestType;

    @Field(name = "date_time")
    private LocalDateTime timestamp;

    public ChildDataStagingRes toChildResData(){
        return new ChildDataStagingRes(
                getId(),
                getEmployeeId(),
                getChildId(),
                getEditedFields(),
                getApprovalStatus(),
                getRequestType(),
                getTimestamp()
        );
    }
}
