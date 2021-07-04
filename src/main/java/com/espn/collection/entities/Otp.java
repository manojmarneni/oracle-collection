package com.espn.collection.entities;


import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name = "otp")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Otp extends BaseEntity{
    @Column(name = "leader_id")
    String leaderId;

    @Column(name = "otp")
    String otp;

    @Column(name = "receive_date")
    Date receiveDate;

    @Column(name = "is_used")
    Boolean used;
}
