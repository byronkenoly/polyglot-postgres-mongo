package com.polyglot.persistence.children.domain;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChildDataStagingRepo extends MongoRepository<ChildDataStaging, UUID> {

    List<ChildDataStaging> findByEmployeeIdAndApprovalStatus(String employeeId, String approvalStatus);

    List<ChildDataStaging> findByApprovalStatus(String approvalStatus);

    List<ChildDataStaging> findByIdIn(List<UUID> ids);

    Optional<ChildDataStaging> findByChildIdAndApprovalStatus(UUID id, String approvalStatus);

    Optional<ChildDataStaging> findByChildIdAndApprovalStatusAndRequestType(UUID id, String approvalStatus, String requestType);
}
