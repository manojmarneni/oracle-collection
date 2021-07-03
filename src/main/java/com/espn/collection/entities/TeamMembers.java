package com.espn.collection.entities;

import lombok.*;

import javax.persistence.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name = "team_members")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamMembers extends BaseEntity {

    @Column(name = "leader_id")
    String leaderId;

    @Column(name = "member_id")
    String memberId;

}
