package com.espn.collection.repository;


import com.espn.collection.entities.TeamMembers;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamMemberRepository extends CrudRepository<TeamMembers, Long> {
    List<TeamMembers> findByLeaderId(String leaderId);
}
