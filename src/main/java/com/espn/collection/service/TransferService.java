package com.espn.collection.service;

import com.espn.collection.email.GmailFetch;
import com.espn.collection.entities.Transaction;
import com.espn.collection.entities.Transfers;
import com.espn.collection.repository.TransactionRepository;
import com.espn.collection.repository.TransfersRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TransferService {

  @Autowired TransactionRepository transactionRepository;

  @Autowired TransfersRepository transfersRepository;
  @Autowired GmailFetch gmailFetch;

  public String transferMoney(String transactionId) {

    Optional<Transaction> optionalTransaction =
        transactionRepository.findById(Long.valueOf(transactionId));
    if (!optionalTransaction.isPresent()) return "INVALID TRANSACTION ID";
    Transaction transaction = optionalTransaction.get();

    List<Transfers> transfers = transfersRepository.findByTransaction(transaction);
    List<Transfers> pendingTransfers = getPendingTransfers(transfers, Transfers.Status.PENDING);

    if (pendingTransfers.isEmpty()) return "NO MORE PENDING MEMBERS";
    Transfers currentTransfer = pendingTransfers.get(0);

    RestTemplate restTemplate = new RestTemplate();

    HttpHeaders headers = getHttpHeaders(transaction.getCsrfToken(), transaction.getCookie(), transaction.getHost());

    String requestBody =
        "withdrawAction=doIncomeTransferMailConfirmation&transferType=Other&receiverWallet=MasterWallet&receiverUsername=withdrawAction=doIncomeTransferMailConfirmation&transferType=Other&receiverWallet=MasterWallet&receiverUsername="
            + currentTransfer.getMemberId()
            + "&baseAmt=10&loginPassword="
            + transaction.getPassword()
            + "&remark=";

    HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
    String ESPN_URL = "https://" + transaction.getHost() + "/withdraw_action";

    ResponseEntity<String> response =
        restTemplate.exchange(ESPN_URL, HttpMethod.POST, request, String.class);
    String otp = null;
    String responseFromOtpSubmission = null;
    if (response.getStatusCode().is2xxSuccessful() && response.getBody().contains("Please check your email. OTP code sent into your mail ")) {
      log.info("Response from otp generation request: {}", response.getBody());
      currentTransfer.setStatus(Transfers.Status.OTP_GENERATED.name());

      try {
        otp = gmailFetch.getOtpForLeader(transaction.getLeaderId());
      } catch (Exception e) {
        e.printStackTrace();
      }
      transfersRepository.save(currentTransfer);
      if (otp != null && otp.length() == 6) {
        responseFromOtpSubmission = transferMoney(transaction.getId().toString(), otp);
      }
    } else {
      log.info("Failure Response from otp generation request: {}", response.getBody());
      currentTransfer.setStatus(Transfers.Status.FAILURE.name());
      transfersRepository.save(currentTransfer);
    }
    return response.getStatusCode().is2xxSuccessful()
        ? "SUCCESS FOR " + currentTransfer.getMemberId() + ".." + responseFromOtpSubmission
        : "FAILED FOR " + currentTransfer.getMemberId();
  }

  public String transferMoney(String transactionId, String otp) {
    log.info("Submitting OTP : {}", otp);
    Optional<Transaction> optionalTransaction =
        transactionRepository.findById(Long.valueOf(transactionId));
    if (!optionalTransaction.isPresent()) return "INVALID TRANSACTION ID";
    Transaction transaction = optionalTransaction.get();
    List<Transfers> transfers = transfersRepository.findByTransaction(transaction);
    List<Transfers> currentTransfers =
        getPendingTransfers(transfers, Transfers.Status.OTP_GENERATED);
    List<Transfers> pendingTransfers = getPendingTransfers(transfers, Transfers.Status.PENDING);

    if (currentTransfers.isEmpty()) return "NO MORE MEMBERS WAITING FOR OTP";

    Transfers currentTransfer = currentTransfers.get(0);

    RestTemplate restTemplate = new RestTemplate();

    HttpHeaders headers = getHttpHeaders(transaction.getCsrfToken(), transaction.getCookie(), transaction.getHost());

    String requestBody =
        "withdrawAction=doIncomeTransfer&transferType=Other&receiverWallet=MasterWallet&receiverUsername="
            + currentTransfer.getMemberId()
            + "&baseAmt=10&loginPassword="
            + transaction.getPassword()
            + "&remark=&otpCode="
            + otp;

    HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
    String ESPN_URL = "https://" + transaction.getHost() + "/withdraw_action";
    ResponseEntity<String> response =
        restTemplate.exchange(ESPN_URL, HttpMethod.POST, request, String.class);

    String nextTransactionResponse = "";

    if (response.getStatusCode().is2xxSuccessful() && response.getBody().contains("Transfer successfully")) {
      log.info("Response from otp submission: {}", response.getBody());
      currentTransfer.setStatus(Transfers.Status.SUCCESS.name());
      transfersRepository.save(currentTransfer);
      nextTransactionResponse = transferMoney(transactionId);
    } else {
      log.info("Failure Response from otp submission: {}", response.getBody());
      currentTransfer.setStatus(Transfers.Status.FAILURE.name());
      transfersRepository.save(currentTransfer);
      for (Transfers pendingTransfer : pendingTransfers) {
        pendingTransfer.setStatus(Transfers.Status.ABORT.name());
        transfersRepository.save(pendingTransfer);
      }
    }

    return response.getStatusCode().is2xxSuccessful()
        ? "SUCCESS FOR " + currentTransfer.getMemberId() + ". Next : " + nextTransactionResponse
        : "FAILED FOR " + currentTransfer.getMemberId();
  }

  private HttpHeaders getHttpHeaders(String csrfToken, String cookie, String host) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("authority", host);
    headers.set(
        "sec-ch-ua",
        "\" Not;A Brand\";v=\"99\", \"Google Chrome\";v=\"91\", \"Chromium\";v=\"91\"");
    headers.set("accept", "application/json, text/javascript, */*; q=0.01");
    headers.set("x-csrf-token", csrfToken);
    headers.set("x-requested-with", "XMLHttpRequest");
    headers.set("sec-ch-ua-mobile", "?0");
    headers.set(
        "user-agent",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.77 Safari/537.36");
    headers.set("content-type", "application/x-www-form-urlencoded; charset=UTF-8");
    headers.set("origin", "https://" + host);
    headers.set("sec-fetch-site", "same-origin");
    headers.set("sec-fetch-mode", "cors");
    headers.set("sec-fetch-dest", "empty");
    headers.set("referer", "https://" + host + "/income_transfer");
    headers.set("'accept-language", "en-US,en;q=0.9,te;q=0.8");
    headers.set("cookie", cookie);
    return headers;
  }

  private List<Transfers> getPendingTransfers(List<Transfers> transfers, Transfers.Status pending) {
    return transfers.stream()
        .filter(transfers1 -> pending.name().equals(transfers1.getStatus()))
        .collect(Collectors.toList());
  }
}
