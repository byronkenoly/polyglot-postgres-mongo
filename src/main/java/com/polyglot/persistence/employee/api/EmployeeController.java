package com.polyglot.persistence.employee.api;

import com.polyglot.persistence.children.data.ChildData;
import com.polyglot.persistence.children.data.ChildHRApprovalData;
import com.polyglot.persistence.children.service.ChildService;
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

    private final ChildService childService;

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

    @PostMapping("/{id}/staged-children")
    public ResponseEntity<?> createChildren(
            @PathVariable("id") UUID id,
            @RequestBody List<ChildData> data
    ){

        childService.createChildren(id, data);

        return ResponseEntity.ok("added child(ren) to staging area successfully.");

    }

    @GetMapping("/{id}/staged-children")
    public ResponseEntity<?> getChildrenForSingleEmployee(
            @PathVariable("id") UUID id
    ){

        List<ChildHRApprovalData> pendingChildren = childService.getPendingChildrenForEmployee(id);

        if (pendingChildren.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(pendingChildren);

    }

    @PatchMapping("/{id}/staged-children/{documentId}")
    public ResponseEntity<?> updateChildReqAwaitingHRApproval(
            @PathVariable("documentId") UUID documentId,
            @PathVariable("id") UUID employeeId,
            @RequestBody Map<String, Object> editedFields
    ){

        childService.editStagedChild(documentId, employeeId, editedFields);

        return ResponseEntity.ok("updated child in staging area successfully");

    }

    @DeleteMapping("/{id}/staged-children/{childId}")
    public ResponseEntity<?> deleteChild(
            @PathVariable("id") UUID id,
            @PathVariable("childId") UUID childId
    ){

        childService.stageChildForDeletion(childId, id);

        return ResponseEntity.ok("delete child request added to staging area");

    }
}
