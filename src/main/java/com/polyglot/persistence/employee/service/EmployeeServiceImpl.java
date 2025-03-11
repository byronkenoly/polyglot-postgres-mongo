package com.polyglot.persistence.employee.service;

import com.polyglot.persistence.employee.data.CreateEmployeeData;
import com.polyglot.persistence.employee.data.EmployeeResponseData;
import com.polyglot.persistence.employee.data.PendingHRApprovalEmployeeData;
import com.polyglot.persistence.employee.domain.Employee;
import com.polyglot.persistence.employee.domain.EmployeeDataStaging;
import com.polyglot.persistence.employee.domain.EmployeeDataStagingRepo;
import com.polyglot.persistence.employee.domain.EmployeeRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService{

    private final EmployeeRepo employeeRepo;
    private final EmployeeDataStagingRepo employeeDataStagingRepo;

    @Transactional
    public void createEmployee(CreateEmployeeData data){

        Employee employee = Employee.builder()
                .firstName(data.firstName())
                .lastName(data.lastName())
                .dob(data.dob())
                .idNo(data.idNo())
                .KRANo(data.KRANo())
                .SHANo(data.SHANo())
                .build();

        employeeRepo.save(employee);
    }

    public List<EmployeeResponseData> getAllEmployees(){

        return employeeRepo.findAll().stream().map(
                Employee::toEmpResData
        ).toList();

    }

    public Employee getEmployee(UUID id){

        return employeeRepo.findById(id).orElseThrow(
                RuntimeException::new
        );
    }

    @Transactional
    public void updateEmployee(UUID id, Map<String, Object> editedFields) {

        boolean isChanged = false;
        Employee employee = getEmployee(id);

        Class<?> clazz = employee.getClass();

        Optional<EmployeeDataStaging> existingPendingDoc = employeeDataStagingRepo.findByEmployeeIdAndApprovalStatus(id.toString(), "PENDING");

        Map<String, Object> mongoEditedFields = existingPendingDoc
                .map(EmployeeDataStaging::getEditedFields)
                .orElse(new HashMap<>());

        for(Map.Entry<String, Object> entry : editedFields.entrySet()){

            String fieldName = entry.getKey();
            Object val = entry.getValue();

            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);

                Object postgresVal = field.get(employee);
                Object mongoVal = mongoEditedFields.get(fieldName);

                if (!mongoEditedFields.containsKey(fieldName)) {
                    mongoEditedFields.put(fieldName, val);
                    isChanged = true;
                } else if (Objects.equals(postgresVal, val)) {
                    mongoEditedFields.remove(fieldName);
                    isChanged = true;
                } else if (!Objects.equals(val, postgresVal) && !Objects.equals(val, mongoVal)) {
                    mongoEditedFields.put(fieldName, val);
                    isChanged = true;
                }

            } catch (NoSuchFieldException e){
                throw new IllegalArgumentException("Field not found: " + fieldName, e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Cannot access field: " + fieldName, e);
            }

        }

        if (isChanged) {
            EmployeeDataStaging empStaging = existingPendingDoc.orElse(
                    EmployeeDataStaging.builder()
                            .id(UUID.randomUUID())
                            .employeeId(id.toString())
                            .editedFields(new HashMap<>())
                            .approvalStatus("PENDING")
                            .timestamp(LocalDateTime.now())
                            .build()
            );

            empStaging.setEditedFields(mongoEditedFields);

            if (mongoEditedFields.isEmpty()) {
                empStaging.setApprovalStatus("CANCELLED");
            }

            employeeDataStagingRepo.save(empStaging);
        }
    }

    public List<PendingHRApprovalEmployeeData> getEmployeeEditsAwaitingHRApproval() {

        List<EmployeeDataStaging> editsPendingApproval = employeeDataStagingRepo.findByApprovalStatus("PENDING");

        List<PendingHRApprovalEmployeeData> pendingApprovalOldnNewVals = new ArrayList<>();

        for (EmployeeDataStaging e : editsPendingApproval) {

            UUID employeeId = UUID.fromString(e.getEmployeeId());

            Employee employee = getEmployee(employeeId);

            Map<String, Object> oldValues = new HashMap<>();
            Map<String, Object> newValues = e.getEditedFields();

            for (String fieldName : newValues.keySet()) {
                try {
                    Field field = Employee.class.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    oldValues.put(fieldName, field.get(employee));
                } catch (NoSuchFieldException | IllegalAccessException ex) {
                    oldValues.put(fieldName, "UNKNOWN FIELD");
                }
            }

            PendingHRApprovalEmployeeData data = new PendingHRApprovalEmployeeData(
                    e.getEmployeeId(),
                    oldValues,
                    newValues,
                    e.getApprovalStatus(),
                    e.getTimestamp()
            );

            pendingApprovalOldnNewVals.add(data);
        }

        return pendingApprovalOldnNewVals;
    }


}
