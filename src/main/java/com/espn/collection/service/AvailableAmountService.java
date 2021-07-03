package com.espn.collection.service;

import com.espn.collection.vo.TransactionRequest;
import org.springframework.stereotype.Service;

@Service
public interface AvailableAmountService {
    Integer getAmount(TransactionRequest transactionRequest);
}
