package com.espn.collection.controller;

import com.espn.collection.email.GmailFetch;
import com.espn.collection.service.TeamMembersService;
import com.espn.collection.vo.TeamMembersRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/members")
public class MembersController {

  @Autowired TeamMembersService teamMembersService;
  @Autowired GmailFetch gmailFetch;

  @PostMapping
  public ResponseEntity membersService(@RequestBody TeamMembersRequest teamMembersRequest) {
    return ResponseEntity.ok(teamMembersService.saveTeamMembers(teamMembersRequest));
  }

  @GetMapping("/otp")
  public ResponseEntity getOtp(@RequestParam String leaderId) {
    try {
      return ResponseEntity.ok(gmailFetch.getOtpForLeader(leaderId));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return ResponseEntity.internalServerError().build();
  }
}
