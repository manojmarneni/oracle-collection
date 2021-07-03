package com.espn.collection.controller;

import com.espn.collection.service.TransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/transfer")
public class TransferController {

  @Autowired TransferService transferService;

  @PostMapping
  @ResponseBody
  public ResponseEntity getFooByIdUsingQueryParam(@RequestParam String transactionid) {
    return ResponseEntity.ok(transferService.transferMoney(transactionid));
  }

  @PostMapping("/otp")
  @ResponseBody
  public ResponseEntity kje(@RequestParam String transactionid, @RequestParam String otp) {
    return ResponseEntity.ok(transferService.transferMoney(transactionid, otp));
  }
}
