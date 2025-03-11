package com.polyglot.persistence.children.service;

import com.polyglot.persistence.children.data.*;
import com.polyglot.persistence.children.domain.Child;
import com.polyglot.persistence.children.domain.ChildDataStaging;
import com.polyglot.persistence.children.domain.ChildDataStagingRepo;
import com.polyglot.persistence.children.domain.ChildRepo;
import com.polyglot.persistence.employee.domain.Employee;
import com.polyglot.persistence.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChildServiceImpl implements ChildService {

    private final ChildRepo childRepo;

    private final ChildDataStagingRepo childDataStagingRepo;

    private final EmployeeService employeeService;

    @Transactional
    public void createChildren(UUID employeeId, List<ChildData> data) {

        Employee employee = employeeService.getEmployee(employeeId);

        List<ChildDataStaging> children = data.stream()
                .map(c -> {
                    Map<String, Object> editedFields = new HashMap<>();
                    editedFields.put("firstName", c.firstName());
                    editedFields.put("lastName", c.lastName());
                    editedFields.put("dob", c.dob());

                    return ChildDataStaging.builder()
                            .id(UUID.randomUUID())
                            .employeeId(employeeId.toString())
                            .editedFields(editedFields)
                            .approvalStatus("PENDING")
                            .requestType("CREATE")
                            .timestamp(LocalDateTime.now())
                            .build();

                })
                .toList();

        childDataStagingRepo.saveAll(children);
    }

    public Child getChild(UUID id){

        return childRepo.findById(id).orElseThrow(
                RuntimeException::new
        );
    }

    public ChildDataStaging getStagedChild(UUID id){

        return childDataStagingRepo.findById(id).orElseThrow(
                RuntimeException::new
        );
    }

    public List<ChildHRApprovalData> getChildrenReqsAwaitingHRApproval() {

        List<ChildDataStaging> reqsPendingApproval = childDataStagingRepo.findByApprovalStatus("PENDING");

        List<ChildHRApprovalData> pendingApprovalData = new ArrayList<>();

        for (ChildDataStaging req : reqsPendingApproval) {
            ChildHRApprovalData dto = createHRApprovalData(req);
            if (dto != null) {
                pendingApprovalData.add(dto);
            }
        }

        return pendingApprovalData;
    }

    private ChildHRApprovalData createHRApprovalData(ChildDataStaging req) {
        return switch (req.getRequestType()) {
            case "CREATE" -> createChildCreateHRApprovalData(req);
            case "EDIT" -> createChildEditHRApprovalData(req);
            case "DELETE" -> createChildDeleteHRApprovalData(req);
            default -> null;
        };
    }

    private ChildCreateHRApprovalData createChildCreateHRApprovalData(ChildDataStaging req) {
        ChildCreateHRApprovalData createDto = new ChildCreateHRApprovalData();
        createDto.setId(req.getId());
        createDto.setEmployeeId(req.getEmployeeId());

        Map<String, Object> createFields = req.getEditedFields();
        createFields.forEach((key, value) -> setFieldValue(createDto, key, value));

        createDto.setRequestType(req.getRequestType());
        createDto.setApprovalStatus(req.getApprovalStatus());
        createDto.setTimestamp(req.getTimestamp());

        return createDto;
    }

    private ChildEditHRApprovalData createChildEditHRApprovalData(ChildDataStaging req) {
        ChildEditHRApprovalData editDto = new ChildEditHRApprovalData();
        editDto.setId(req.getId());
        editDto.setEmployeeId(req.getEmployeeId());
        editDto.setChildId(UUID.fromString((String) req.getEditedFields().get("childId")));
        editDto.setEditedFields(req.getEditedFields());
        editDto.setRequestType(req.getRequestType());
        editDto.setApprovalStatus(req.getApprovalStatus());
        editDto.setTimestamp(req.getTimestamp());

        return editDto;
    }

    private ChildDeleteHRApprovalData createChildDeleteHRApprovalData(ChildDataStaging req) {
        ChildDeleteHRApprovalData deleteDto = new ChildDeleteHRApprovalData();
        deleteDto.setId(req.getId());
        deleteDto.setEmployeeId(req.getEmployeeId());
        deleteDto.setRequestType(req.getRequestType());
        deleteDto.setChildId(UUID.fromString(req.getChildId()));
        deleteDto.setApprovalStatus(req.getApprovalStatus());
        deleteDto.setTimestamp(req.getTimestamp());

        return deleteDto;
    }

    private void setFieldValue(Object dto, String fieldName, Object value) {
        try {
            Field field = dto.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(dto, value);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }
    }

    public void editStagedChild(UUID id, UUID employeeId, Map<String, Object> editedFields){

        ChildDataStaging child = getStagedChild(id);

        if ("CREATE".equals(child.getRequestType())){
            Map<String, Object> existingFields = child.getEditedFields();
            existingFields.putAll(editedFields);

            child.setEditedFields(existingFields);
            child.setTimestamp(LocalDateTime.now());

            childDataStagingRepo.save(child);
        } else if ("EDIT".equals(child.getRequestType())){
            if (child.getChildId() == null) {
                throw new IllegalArgumentException("EDIT request must have a childId.");
            }
            UUID childId = UUID.fromString(child.getChildId());

            Optional<ChildDataStaging> existingDeleteRequest = childDataStagingRepo.findByChildIdAndApprovalStatusAndRequestType(childId, "PENDING", "DELETE");

            if (existingDeleteRequest.isPresent()) {
                throw new IllegalStateException("Cannot edit child. A pending delete request exists.");
            }

            Child existingChild = getChild(childId);

            Optional<ChildDataStaging> existingPendingEditOpt = childDataStagingRepo.findByChildIdAndApprovalStatus(childId, "PENDING");
            ChildDataStaging existingPendingEdit = existingPendingEditOpt.get();

            Map<String, Object> mongoEditedFields = existingPendingEdit.getEditedFields();

            boolean isChanged = false;
            Class<?> clazz = existingChild.getClass();

            for (Map.Entry<String, Object> entry : editedFields.entrySet()) {
                String fieldName = entry.getKey();
                Object newValue = entry.getValue();

                try {
                    Field field = clazz.getDeclaredField(fieldName);
                    field.setAccessible(true);

                    Object postgresValue = field.get(existingChild);
                    Object mongoValue = mongoEditedFields.get(fieldName);

                    if (!mongoEditedFields.containsKey(fieldName)) {
                        mongoEditedFields.put(fieldName, newValue);
                        isChanged = true;
                    } else if (Objects.equals(postgresValue, newValue)) {
                        mongoEditedFields.remove(fieldName);
                        isChanged = true;
                    } else if (!Objects.equals(newValue, postgresValue) && !Objects.equals(newValue, mongoValue)) {
                        mongoEditedFields.put(fieldName, newValue);
                        isChanged = true;
                    }

                } catch (NoSuchFieldException e) {
                    throw new IllegalArgumentException("Field not found: " + fieldName, e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Cannot access field: " + fieldName, e);
                }
            }

            if (isChanged) {
                existingPendingEdit.setEditedFields(mongoEditedFields);
                if (mongoEditedFields.isEmpty()) {
                    existingPendingEdit.setApprovalStatus("CANCELLED");
                }
                childDataStagingRepo.save(existingPendingEdit);
            }

        }
    }

//    //CREATE MODE - Child doesn't exist in Postgres
//    public void editPendingChild(UUID id, Map<String, Object> editedFields){
//
//        ChildDataStaging child = getStagedChild(id);
//
//        Map<String, Object> existingFields = child.getEditedFields();
//        existingFields.putAll(editedFields);
//
//        child.setEditedFields(existingFields);
//        child.setTimestamp(LocalDateTime.now());
//
//        childDataStagingRepo.save(child);
//    }
//
//    //id - child id from postgres
//    public void editExistingChild(UUID id, UUID employeeId, Map<String, Object> editedFields){
//
//        Optional<ChildDataStaging> existingDeleteRequest = childDataStagingRepo.findByChildIdAndApprovalStatusAndRequestType(id, "PENDING", "DELETE");
//
//        if (existingDeleteRequest.isPresent()) {
//            throw new IllegalStateException("Cannot edit child. A pending delete request exists.");
//        }
//
//        Child existingChild = getChild(id);
//
//        Optional<ChildDataStaging> existingPendingEdit = childDataStagingRepo.findByChildIdAndApprovalStatus(id, "PENDING");
//
//        Map<String, Object> mongoEditedFields = existingPendingEdit
//                .map(ChildDataStaging::getEditedFields)
//                .orElse(new HashMap<>());
//
//        boolean isChanged = false;
//        Class<?> clazz = existingChild.getClass();
//
//        for (Map.Entry<String, Object> entry : editedFields.entrySet()) {
//            String fieldName = entry.getKey();
//            Object newValue = entry.getValue();
//
//            try {
//                Field field = clazz.getDeclaredField(fieldName);
//                field.setAccessible(true);
//
//                Object postgresValue = field.get(existingChild);
//                Object mongoValue = mongoEditedFields.get(fieldName);
//
//                if (!mongoEditedFields.containsKey(fieldName)) {
//                    mongoEditedFields.put(fieldName, newValue);
//                    isChanged = true;
//                } else if (Objects.equals(postgresValue, newValue)) {
//                    mongoEditedFields.remove(fieldName);
//                    isChanged = true;
//                } else if (!Objects.equals(newValue, postgresValue) && !Objects.equals(newValue, mongoValue)) {
//                    mongoEditedFields.put(fieldName, newValue);
//                    isChanged = true;
//                }
//
//            } catch (NoSuchFieldException e) {
//                throw new IllegalArgumentException("Field not found: " + fieldName, e);
//            } catch (IllegalAccessException e) {
//                throw new RuntimeException("Cannot access field: " + fieldName, e);
//            }
//        }
//
//        if (isChanged) {
//            ChildDataStaging editRequest = existingPendingEdit.orElse(
//                    ChildDataStaging.builder()
//                            .id(UUID.randomUUID())
//                            .employeeId(employeeId.toString())
//                            .childId(id.toString())
//                            .requestType("EDIT")
//                            .editedFields(new HashMap<>())
//                            .approvalStatus("PENDING")
//                            .timestamp(LocalDateTime.now())
//                            .build()
//            );
//
//            editRequest.setEditedFields(mongoEditedFields);
//
//            if (mongoEditedFields.isEmpty()) {
//                editRequest.setApprovalStatus("CANCELLED");
//            }
//
//            childDataStagingRepo.save(editRequest);
//        }
//    }

    public void stageChildForDeletion(UUID childId, UUID employeeId) {

        Optional<ChildDataStaging> existingPendingDelete = childDataStagingRepo.findByChildIdAndApprovalStatusAndRequestType(childId, "PENDING", "DELETE");

        if (existingPendingDelete.isPresent()) {
            throw new IllegalStateException("A pending delete request already exists for this child.");
        }

        Optional<ChildDataStaging> existingPendingEdit = childDataStagingRepo.findByChildIdAndApprovalStatusAndRequestType(childId, "PENDING", "EDIT");

        existingPendingEdit.ifPresent(childDataStagingRepo::delete);

        ChildDataStaging deleteRequest = ChildDataStaging.builder()
                .id(UUID.randomUUID())
                .employeeId(employeeId.toString())
                .childId(childId.toString())
                .requestType("DELETE")
                .approvalStatus("PENDING")
                .timestamp(LocalDateTime.now())
                .build();

        childDataStagingRepo.save(deleteRequest);
    }

    //HR Approval or rejection
    public void reviewChildRequests(List<UUID> ids, String action){

        List<ChildDataStaging> pendingReqs = childDataStagingRepo.findByIdIn(ids);

        List<Child> childrenToSave = new ArrayList<>();
        List<ChildDataStaging> rejectedRecords = new ArrayList<>();

        for (ChildDataStaging req : pendingReqs){

            if ("APPROVE".equalsIgnoreCase(action)){
                processApproval(req, childrenToSave);
            } else if ("REJECT".equalsIgnoreCase(action)){
                req.setApprovalStatus("CANCELLED");
                rejectedRecords.add(req);
            } else {
                throw new IllegalArgumentException("Invalid action. Use 'APPROVE' or 'REJECT'.");
            }

            if (!childrenToSave.isEmpty()) {
                childRepo.saveAll(childrenToSave);
            }

            if (!rejectedRecords.isEmpty()) {
                childDataStagingRepo.saveAll(rejectedRecords);
            }
        }
    }

    private void processApproval(ChildDataStaging req, List<Child> childrenToSave) {
        switch (req.getRequestType()) {
            case "CREATE" -> {
                Child newChild = Child.builder().build();
                updateEntityFields(newChild, req.getEditedFields());
                childrenToSave.add(newChild);
            }
            case "EDIT" -> {
                UUID postgresId = UUID.fromString((String) req.getEditedFields().get("childId"));
                Child existingChild = getChild(postgresId);

                updateEntityFields(existingChild, req.getEditedFields());
                childRepo.save(existingChild);
            }
            case "DELETE" -> {
                UUID postgresId = UUID.fromString(req.getChildId());
                childRepo.deleteById(postgresId);
            }
            default -> throw new IllegalArgumentException("Unknown request type: " + req.getRequestType());
        }

        req.setApprovalStatus("APPROVED");
        childDataStagingRepo.save(req);
    }

    private void updateEntityFields(Object entity, Map<String, Object> editedFields) {
        Class<?> clazz = entity.getClass();

        for (Map.Entry<String, Object> entry : editedFields.entrySet()) {
            try {
                Field field = clazz.getDeclaredField(entry.getKey());
                field.setAccessible(true);
                field.set(entity, entry.getValue());
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("Error updating field: " + entry.getKey(), e);
            }
        }
    }

    public List<ChildHRApprovalData> getPendingChildrenForEmployee(UUID employeeId) {
        List<ChildDataStaging> pendingRequests = childDataStagingRepo.findByEmployeeIdAndApprovalStatus(employeeId.toString(), "PENDING");

        return pendingRequests.stream()
                .map(this::createHRApprovalData)
                .filter(Objects::nonNull)
                .toList();
    }

}