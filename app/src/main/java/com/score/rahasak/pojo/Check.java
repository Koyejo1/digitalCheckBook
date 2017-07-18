package com.score.rahasak.pojo;

import com.score.senzc.pojos.User;

/**
 * Created by Lakmal on 7/15/17.
 */

public class Check {
    private String checkUrl;
    private BankUser bankUser;
    private Long timeCreated;
    private Long amount;
    private SecretUser sender;


    public Check(BankUser bankUser, String checkUrl, Long timestamp, Long amount, SecretUser sender){
        this.checkUrl = checkUrl;
        this.bankUser = bankUser;
        this.timeCreated = timestamp;
        this.amount = amount;
        this.sender = sender;
    }

    public String getCheckUrl() {
        return checkUrl;
    }

    public BankUser getBankUser() {
        return bankUser;
    }

    public Long getTimeCreated() {
        return timeCreated;
    }

    public Long getAmount() {
        return amount;
    }

    public SecretUser getSender() {
        return sender;
    }
}
