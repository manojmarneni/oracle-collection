package com.espn.collection.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name = "transactions")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Transaction extends BaseEntity {

    @Column(name = "leader_id")
    String leaderId;

    @Column(name = "csrf_token")
    String csrfToken;

    @Column(name = "cookie")
    String cookie;

    @Column(name = "status")
    String status;

    @Column(name = "password")
    String password;

    @OneToMany(mappedBy="transaction")
    private Set<Transfers> transfers;
}
