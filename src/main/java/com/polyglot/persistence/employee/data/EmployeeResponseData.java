package com.polyglot.persistence.employee.data;

import java.util.Date;
import java.util.UUID;

public record EmployeeResponseData(
        UUID id,
        String firstName,
        String lastName,
        Date dob,
        String idNo,
        String KRANo,
        String SHANo
) {
}
