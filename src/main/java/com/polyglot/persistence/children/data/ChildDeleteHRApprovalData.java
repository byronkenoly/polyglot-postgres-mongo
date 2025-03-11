package com.polyglot.persistence.children.data;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ChildDeleteHRApprovalData extends ChildHRApprovalData{

    private UUID childId;
}
