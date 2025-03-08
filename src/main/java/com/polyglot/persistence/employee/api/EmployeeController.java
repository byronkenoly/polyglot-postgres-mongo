package com.polyglot.persistence.employee.api;

import com.polyglot.persistence.employee.data.CreateEmployeeData;
import com.polyglot.persistence.employee.data.EmployeeResponseData;
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

    @GetMapping
    public ResponseEntity<?> getAllEmployees(){

        List<EmployeeResponseData> empResData = employeeService.getAllEmployees();

        return ResponseEntity.ok(empResData);
    }

    @PostMapping
    public ResponseEntity<?> createEmployee(
            @RequestBody CreateEmployeeData createEmployeeData
    ) {
        employeeService.createEmployee(createEmployeeData);
        return ResponseEntity.ok("created employee successfully");
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateEmployee(
            @PathVariable("id") UUID id,
            @RequestBody Map<String, Object> editedFields
    ){
        employeeService.updateEmployee(id, editedFields);
        return ResponseEntity.ok("added edited data to staging area");
    }
}
