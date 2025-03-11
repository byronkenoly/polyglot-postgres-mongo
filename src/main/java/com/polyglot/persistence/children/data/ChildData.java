package com.polyglot.persistence.children.data;

import java.util.Date;

public record ChildData(
        String firstName,
        String lastName,
        Date dob
) {
}
