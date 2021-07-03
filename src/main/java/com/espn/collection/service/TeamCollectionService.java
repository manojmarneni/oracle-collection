package com.espn.collection.service;

import com.espn.collection.vo.TransactionRequest;
import org.springframework.stereotype.Service;

@Service
public interface TeamCollectionService {
    Integer collectAmount(TransactionRequest transactionRequest);

}
