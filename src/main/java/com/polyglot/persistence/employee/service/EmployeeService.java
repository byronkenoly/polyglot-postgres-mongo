package com.polyglot.persistence.employee.service;

import com.polyglot.persistence.employee.data.CreateEmployeeData;
import com.polyglot.persistence.employee.data.EmployeeResponseData;
import com.polyglot.persistence.employee.data.PendingHRApprovalEmployeeData;
import com.polyglot.persistence.employee.domain.Employee;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface EmployeeService {

    void createEmployee(CreateEmployeeData data);

    List<EmployeeResponseData> getAllEmployees();

    Employee getEmployee(UUID employeeId);

    void updateEmployee(UUID employeeId, Map<String, Object> editedFields);

    List<PendingHRApprovalEmployeeData> getEmployeeEditsAwaitingHRApproval();
}
