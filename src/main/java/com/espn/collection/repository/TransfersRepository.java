package com.espn.collection.repository;

import com.espn.collection.entities.Transaction;
import com.espn.collection.entities.Transfers;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransfersRepository extends CrudRepository<Transfers, Long> {
    List<Transfers> findByTransaction(Transaction transaction);
    List<Transfers> findByStatus(String transactionId);
}
