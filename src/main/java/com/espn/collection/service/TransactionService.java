package com.espn.collection.service;

import com.espn.collection.entities.TeamMembers;
import com.espn.collection.entities.Transaction;
import com.espn.collection.entities.Transfers;
import com.espn.collection.repository.TeamMemberRepository;
import com.espn.collection.repository.TransactionRepository;
import com.espn.collection.repository.TransfersRepository;
import com.espn.collection.vo.TransactionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class TransactionService {

  public static final int TRANSFER_AMOUNT = 10;
  @Autowired TeamMemberRepository teamMemberRepository;

  //  @Autowired TransferService transferService;
  //  @Autowired TransferEligibleMemberService transferEligibleMemberService;
  //  @Autowired AvailableAmountService availableAmountService;
  //  @Autowired TeamCollectionService teamCollectionService;
  @Autowired TransfersRepository transfersRepository;
  @Autowired TransactionRepository transactionRepository;

  @Transactional
  public String triggerCollection(TransactionRequest transactionRequest) {

    List<TeamMembers> teamMembers =
        teamMemberRepository.findByLeaderId(transactionRequest.getLeaderId());

    //    Set<String> transferEligibleMembers =
    //        transferEligibleMemberService.getMembers(transactionRequest);
    //
    //    Integer currentAmount = availableAmountService.getAmount(transactionRequest);

    Transaction transaction =
        transactionRepository.save(
            Transaction.builder()
                .leaderId(transactionRequest.getLeaderId())
                .csrfToken(transactionRequest.getCsrfToken())
                .cookie(transactionRequest.getCookie())
                .password(transactionRequest.getPassword())
                .build());
    for (TeamMembers teamMember : teamMembers) {
      //      if (!transferEligibleMembers.contains(teamMember.getMemberId())) continue;

      //      currentAmount = currentAmount - TRANSFER_AMOUNT;
      //      if (currentAmount < TRANSFER_AMOUNT) {
      //        currentAmount = teamCollectionService.collectAmount(transactionRequest);
      //      }

      transfersRepository.save(
          Transfers.builder()
              .transaction(transaction)
              .leaderId(teamMember.getLeaderId())
              .memberId(teamMember.getMemberId())
              .status(Transfers.Status.PENDING.name())
              .build());
    }
    return transaction.getId().toString();
  }
}
