package com.polyglot.persistence.employee.domain;

import com.polyglot.persistence.employee.data.EmployeeResponseData;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "employee")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "dob")
    private Date dob;

    @Column(name = "id_number")
    private String idNo;

    @Column(name = "kra_number")
    private String KRANo;

    @Column(name = "sha_number")
    private String SHANo;

    public EmployeeResponseData toEmpResData(){

        return new EmployeeResponseData(
                getId(),
                getFirstName(),
                getLastName(),
                getDob(),
                getIdNo(),
                getKRANo(),
                getSHANo()
        );
    }
}
