package com.polyglot.persistence.children.data;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class ChildEditHRApprovalData extends ChildHRApprovalData{

    private UUID childId;
    private Map<String, Object> editedFields;
}
