package com.espn.collection.entities;

import lombok.*;

import javax.persistence.*;


@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name = "transfers")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Transfers extends BaseEntity{
    @ManyToOne
    @JoinColumn(name="transaction_id", nullable=false)
    private Transaction transaction;

    @Column(name = "leader_id")
    String leaderId;

    @Column(name = "member_id")
    String memberId;

    @Column(name = "status")
    String status;

    public enum Status{
        PENDING, OTP_GENERATED, SUCCESS, FAILURE, ABORT
    }
}
