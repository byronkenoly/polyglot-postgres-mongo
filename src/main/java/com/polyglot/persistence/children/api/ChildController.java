package com.polyglot.persistence.children.api;

import com.polyglot.persistence.children.data.ChildData;
import com.polyglot.persistence.children.data.ChildHRApprovalData;
import com.polyglot.persistence.children.domain.Child;
import com.polyglot.persistence.children.domain.ChildDataStaging;
import com.polyglot.persistence.children.service.ChildService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/children")
public class ChildController {

    private final ChildService childService;

    @GetMapping("/pending-reqs")
    public ResponseEntity<?> getChildrenReqsAwaitingHRApproval(){

        List<ChildHRApprovalData> pendingReqs = childService.getChildrenReqsAwaitingHRApproval();

        return ResponseEntity.ok(pendingReqs);
    }

    @PostMapping("/pending-reqs")
    public ResponseEntity<?> reviewChildReqsAwaitingHRApproval(
                @RequestBody List<UUID> ids,
                @RequestParam("action") String action
    ){
        childService.reviewChildRequests(ids, action);
        return ResponseEntity.ok(String.format("successfully processed action: %s for selected child(ren) documents with IDs: %s", action, ids));
    }

//    @GetMapping("/pending-reqs/{id}")
//    public ResponseEntity<?> getChildrenReqAwaitingHRApproval(
//            @PathVariable UUID id
//    ){
//        ChildDataStaging child = childService.getStagedChild(id);
//
//        ChildDataStaging data = ChildDataStaging.builder()
//                .id(child.getId())
//                .employeeId(child.getEmployeeId())
//                .childId(child.getChildId())
//                .editedFields(child.getEditedFields())
//                .approvalStatus(child.getApprovalStatus())
//                .requestType(child.getRequestType())
//                .timestamp(child.getTimestamp())
//                .build();
//
//        return ResponseEntity.ok(data);
//    }
//
//    @PatchMapping("/pending-reqs/{id}")
//    public ResponseEntity<?> updateChildReqAwaitingHRApproval(
//            @PathVariable UUID id,
//            @RequestBody Map<String, Object> editedFields
//    ){
//
//        childService.editPendingChild(id, editedFields);
//
//        return ResponseEntity.ok(String.format("updated child of id: %s in staging area successfully", id));
//    }
}
