package com.polyglot.persistence.employee.domain;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface EmployeeDataStagingRepo extends MongoRepository<EmployeeDataStaging, UUID> {
}