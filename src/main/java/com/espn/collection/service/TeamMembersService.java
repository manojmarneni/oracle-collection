package com.espn.collection.service;

import com.espn.collection.entities.TeamMembers;
import com.espn.collection.repository.TeamMemberRepository;
import com.espn.collection.vo.TeamMembersRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class TeamMembersService {

  @Autowired TeamMemberRepository teamMemberRepository;

  public String saveTeamMembers(TeamMembersRequest teamMembersRequest) {
    List<String> members = Arrays.asList(teamMembersRequest.getMembers().split(","));
    for (String member : members) {
      teamMemberRepository.save(
          TeamMembers.builder()
              .leaderId(teamMembersRequest.getLeaderId().trim())
              .memberId(member.trim())
              .build());
    }
    return "DONE";
  }
}
