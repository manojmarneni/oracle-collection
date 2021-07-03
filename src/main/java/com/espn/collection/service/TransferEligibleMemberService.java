package com.espn.collection.service;

import com.espn.collection.vo.TransactionRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public interface TransferEligibleMemberService {
    Set<String> getMembers(TransactionRequest transactionRequest);

}
