package com.espn.collection.controller;

import com.espn.collection.service.TeamMembersService;
import com.espn.collection.vo.TeamMembersRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/members")
public class MembersController {

  @Autowired TeamMembersService teamMembersService;

  @PostMapping
  public ResponseEntity membersService(@RequestBody TeamMembersRequest teamMembersRequest) {
    return ResponseEntity.ok(teamMembersService.saveTeamMembers(teamMembersRequest));
  }
}
