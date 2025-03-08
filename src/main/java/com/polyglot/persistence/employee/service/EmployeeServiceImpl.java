package com.polyglot.persistence.employee.service;

import com.polyglot.persistence.employee.data.CreateEmployeeData;
import com.polyglot.persistence.employee.data.EmployeeResponseData;
import com.polyglot.persistence.employee.domain.Employee;
import com.polyglot.persistence.employee.domain.EmployeeDataStaging;
import com.polyglot.persistence.employee.domain.EmployeeDataStagingRepo;
import com.polyglot.persistence.employee.domain.EmployeeRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

        for(Map.Entry<String, Object> entry : editedFields.entrySet()){

            String fieldName = entry.getKey();
            Object val = entry.getValue();

            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);

                Object oldVal = field.get(employee);

                if (val != null && !oldVal.equals(val)) {
                    field.set(employee, val);
                    isChanged = true;
                }
            } catch (NoSuchFieldException e){
                throw new IllegalArgumentException("Field not found: " + fieldName, e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Cannot access field: " + fieldName, e);
            }

        }

        if (isChanged) {
            EmployeeDataStaging empStaging = EmployeeDataStaging.builder()
                    .id(UUID.randomUUID())
                    .employeeId(id.toString())
                    .editedFields(editedFields)
                    .approvalStatus("PENDING")
                    .timestamp(LocalDateTime.now())
                    .build();

            employeeDataStagingRepo.save(empStaging);
        }
    }


}
