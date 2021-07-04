package com.espn.collection.repository;

import com.espn.collection.entities.Otp;
import org.springframework.data.repository.CrudRepository;

public interface OtpRepository extends CrudRepository<Otp, Long> {
  Otp findOneByLeaderIdAndUsed(String leaderId, Boolean used);
}
