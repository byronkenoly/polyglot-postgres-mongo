package com.polyglot.persistence.children.data;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ChildCreateHRApprovalData extends ChildHRApprovalData{

    private String firstName;
    private String lastName;
    private Date dob;
}
