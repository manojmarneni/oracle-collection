package com.espn.collection.email;

import com.espn.collection.entities.Otp;
import com.espn.collection.repository.OtpRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;
import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class GmailFetch {

  @Autowired OtpRepository otpRepository;

  public static final String OTP_PARSE_STRING = "OTP Code :</span> <span>";
  public static final String OTP_CODE_FROM_E_ORACLE_SUBJECT = "OTP code from e-Oracle";
  public static final int OTP_LENGTH = 6;
  public static final String LEADER_NAME_PARSE_STRING = ">Dear,";

  @Value("${email}")
  private String email;

  @Value("${password}")
  private String password;

  @Scheduled(initialDelay = 10000, fixedDelay = 2000)
  public String readRecentOtp() throws Exception {
    Session session = Session.getDefaultInstance(new Properties());
    Store store = session.getStore("imaps");
    store.connect("imap.googlemail.com", 993, email, password);
    log.info("Reading OTPs");
    Folder inbox = store.getFolder("INBOX");
    inbox.open(Folder.READ_WRITE);

    // Fetch unseen messages from inbox folder
    Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
    sortMessagesByRecent(messages);

    for (Message message : messages) {

      if (!(message.getSubject().contains("OTP")
          && message.getSubject().contains("code")
          && message.getSubject().contains("from")
          && message.getSubject().contains("e-Oracle"))) {
        continue;
      }

      String messageBody =
          ((MimeMultipart) message.getContent()).getBodyPart(1).getContent().toString();

      String otp = getOtp(messageBody);
      String leaderIdInMail = getLeaderId(messageBody);

      otpRepository.save(
          Otp.builder()
              .leaderId(leaderIdInMail.toLowerCase())
              .otp(otp)
              .receiveDate(message.getReceivedDate())
              .used(false)
              .build());
      message.setFlag(Flags.Flag.SEEN, true);
    }
    log.info("Updated OTPs");
    inbox.close(false);
    store.close();
    return null;
  }

  private void sortMessagesByRecent(Message[] messages) {
    // Sort messages from recent to oldest
    Arrays.sort(
        messages,
        (m1, m2) -> {
          try {
            return m2.getSentDate().compareTo(m1.getSentDate());
          } catch (MessagingException e) {
            throw new RuntimeException(e);
          }
        });
  }

  private static String getLeaderId(String messageBody) {

    int index = messageBody.lastIndexOf(LEADER_NAME_PARSE_STRING);
    messageBody = messageBody.substring(index - 10, index + 1000);

    String pattern = "(.*)>Dear,\\s(.*)</div>(.*)";
    Pattern r = Pattern.compile(pattern);
    Matcher m = r.matcher(messageBody);
    if (m.find()) {
      log.info("Loading LEADER: {}", m.group(2));
      return m.group(2);
    } else {
      log.info("NO LEADER ID IN MESSAGE");
    }
    return "NOT_FOUND";
  }

  private static String getOtp(String messageBody) {

    int index = messageBody.lastIndexOf(OTP_PARSE_STRING);

    return messageBody.substring(
        index + OTP_PARSE_STRING.length(), index + OTP_PARSE_STRING.length() + OTP_LENGTH);
  }

  public String getOtpForLeader(String leaderId) throws Exception {
    int count = 0;
    while (count < 12) {
      Thread.sleep(5000);
      log.info("Checking OTP ");

      Otp otpEntry = otpRepository.findOneByLeaderIdAndUsed(leaderId.toLowerCase(), false);
      if (otpEntry != null) {
        otpEntry.setUsed(true);
        otpRepository.save(otpEntry);
        log.info("FOUND OTP : {} ", otpEntry.getOtp());
        return otpEntry.getOtp();
      }

      count++;
    }
    log.info("NOT FOUND OTP FOR LEADER : {} ", leaderId);
    return null;
  }
}
