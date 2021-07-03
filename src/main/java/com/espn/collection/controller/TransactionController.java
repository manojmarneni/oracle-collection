package com.espn.collection.controller;

import com.espn.collection.service.TransactionService;
import com.espn.collection.vo.TransactionRequest;
import com.espn.collection.vo.TransactionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/transaction")
public class TransactionController {

  @Autowired TransactionService transactionService;

  @PostMapping(value = "/order", consumes = "application/json", produces = "application/json")
  public ResponseEntity createPerson(@RequestBody TransactionRequest transactionRequest) {
    return ResponseEntity.ok(transactionService.triggerCollection(transactionRequest));
  }
}
