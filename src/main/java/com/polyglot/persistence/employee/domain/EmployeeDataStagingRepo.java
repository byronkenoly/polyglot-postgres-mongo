package com.polyglot.persistence.employee.domain;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeDataStagingRepo extends MongoRepository<EmployeeDataStaging, UUID> {

    List<EmployeeDataStaging> findByApprovalStatus(String approvalStatus);

    Optional<EmployeeDataStaging> findByEmployeeIdAndApprovalStatus(String employeeId, String approvalStatus);
}