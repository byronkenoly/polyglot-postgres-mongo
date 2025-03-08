package com.polyglot.persistence.employee.data;

import java.util.Date;
import java.util.Map;

public record CreateEmployeeData(
        String firstName,
        String lastName,
        Date dob,
        String idNo,
        String KRANo,
        String SHANo
) {
}
