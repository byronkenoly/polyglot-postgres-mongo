package com.polyglot.persistence.children.service;

import com.polyglot.persistence.children.data.ChildData;
import com.polyglot.persistence.children.data.ChildHRApprovalData;
import com.polyglot.persistence.children.domain.ChildDataStaging;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ChildService {

    void createChildren(UUID employeeId, List<ChildData> data);

    ChildDataStaging getStagedChild(UUID id);

    List<ChildHRApprovalData> getChildrenReqsAwaitingHRApproval();

    void editStagedChild(UUID id, UUID employeeId, Map<String, Object> editedFields);

    void reviewChildRequests(List<UUID> ids, String action);

    void stageChildForDeletion(UUID childId, UUID employeeId);

    List<ChildHRApprovalData> getPendingChildrenForEmployee(UUID employeeId);
}
