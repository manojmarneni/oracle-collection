package com.espn.collection.service;

import com.espn.collection.entities.Transaction;
import com.espn.collection.entities.Transfers;
import com.espn.collection.repository.TransactionRepository;
import com.espn.collection.repository.TransfersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TransferService {

  @Autowired TransactionRepository transactionRepository;

  @Autowired TransfersRepository transfersRepository;

  public static final String ESPN_URL = "fooResourceUrl";

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

    HttpHeaders headers = getHttpHeaders(transaction.getCsrfToken(), transaction.getCookie());

    String requestBody =
        "withdrawAction=doIncomeTransferMailConfirmation&transferType=Other&receiverWallet=MasterWallet&receiverUsername="
            + currentTransfer.getMemberId()
            + "&baseAmt=10&loginPassword="
            + transaction.getPassword()
            + "&remark=";

    HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

    ResponseEntity<String> response =
        restTemplate.exchange(ESPN_URL, HttpMethod.POST, request, String.class);

    if (response.getStatusCode().is2xxSuccessful()) {
      currentTransfer.setStatus(Transfers.Status.OTP_GENERATED.name());
      transfersRepository.save(currentTransfer);
    } else {
      currentTransfer.setStatus(Transfers.Status.FAILURE.name());
      transfersRepository.save(currentTransfer);
    }
    return response.getStatusCode().is2xxSuccessful()
        ? "SUCCESS FOR " + currentTransfer.getMemberId()
        : "FAILED FOR " + currentTransfer.getMemberId();
  }

  public String transferMoney(String transactionId, String otp) {

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

    HttpHeaders headers = getHttpHeaders(transaction.getCsrfToken(), transaction.getCookie());

    String requestBody =
        "doIncomeTransfer&transferType=Other&receiverWallet=MasterWallet&receiverUsername="
            + currentTransfer.getMemberId()
            + "&baseAmt=10&loginPassword="
            + transaction.getPassword()
            + "&remark=&otpCode="
            + otp;

    HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

    ResponseEntity<String> response =
        restTemplate.exchange(ESPN_URL, HttpMethod.POST, request, String.class);

    if (response.getStatusCode().is2xxSuccessful()) {
      currentTransfer.setStatus(Transfers.Status.SUCCESS.name());
      transfersRepository.save(currentTransfer);
      transferMoney(transactionId);
    } else {
      currentTransfer.setStatus(Transfers.Status.FAILURE.name());
      transfersRepository.save(currentTransfer);
      for (Transfers pendingTransfer : pendingTransfers) {
        pendingTransfer.setStatus(Transfers.Status.ABORT.name());
        transfersRepository.save(pendingTransfer);
      }
    }

    return response.getStatusCode().is2xxSuccessful()
        ? "SUCCESS FOR " + currentTransfer.getMemberId()
        : "FAILED FOR " + currentTransfer.getMemberId();
  }

  private HttpHeaders getHttpHeaders(String csrfToken, String cookie) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("authority", "daffodil.e-oracle.com");
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
    headers.set("origin", "https://daffodil.e-oracle.com");
    headers.set("sec-fetch-site", "same-origin");
    headers.set("sec-fetch-mode", "cors");
    headers.set("sec-fetch-dest", "empty");
    headers.set("referer", "https://daffodil.e-oracle.com/income_transfer");
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
