package com.espn.collection.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NonNull;

@Data
public class TransactionRequest  {

    @JsonProperty("leader_id")
    @NonNull
    String leaderId;

    @JsonProperty("csrf_token")
    @NonNull
    String csrfToken;

    @JsonProperty("cookie")
    @NonNull
    String cookie;

    @JsonProperty("password")
    @NonNull
    String password;
    @JsonProperty("host")
    @NonNull
    String host;

}
