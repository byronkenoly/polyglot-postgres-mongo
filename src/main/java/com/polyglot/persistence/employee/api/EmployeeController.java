package com.polyglot.persistence.employee.api;

import com.polyglot.persistence.employee.data.CreateEmployeeData;
import com.polyglot.persistence.employee.data.EmployeeResponseData;
import com.polyglot.persistence.employee.data.PendingHRApprovalEmployeeData;
import com.polyglot.persistence.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<?> createEmployee(
            @RequestBody CreateEmployeeData createEmployeeData
    ) {
        employeeService.createEmployee(createEmployeeData);
        return ResponseEntity.ok("created employee successfully");
    }

    @GetMapping
    public ResponseEntity<?> getAllEmployees(){

        List<EmployeeResponseData> empResData = employeeService.getAllEmployees();

        return ResponseEntity.ok(empResData);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateEmployee(
            @PathVariable("id") UUID id,
            @RequestBody Map<String, Object> editedFields
    ){
        employeeService.updateEmployee(id, editedFields);
        return ResponseEntity.ok("added edited data to staging area");
    }

    @GetMapping("/pending-edits")
    public ResponseEntity<?> getEmployeeEditsAwaitingHRApproval(){

        List<PendingHRApprovalEmployeeData> pendingEdits = employeeService.getEmployeeEditsAwaitingHRApproval();

        return ResponseEntity.ok(pendingEdits);
    }
}
