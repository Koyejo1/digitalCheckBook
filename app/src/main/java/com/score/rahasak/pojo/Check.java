package com.score.rahasak.pojo;

import com.score.senzc.pojos.User;

/**
 * Created by Lakmal on 7/15/17.
 */

public class Check {
    private String id;
    private String fullName;
    private String signatureUrl;
    private SecretUser issuedTo;
    private SecretUser issuedFrom;
    private Long createdAt;
    private Long amount;

    public Check(String id, SecretUser issuedTo, SecretUser issuedFrom, String fullName, String signatureUrl, Long amount, Long createAtTimeStamp){
        this.id = id;
        this.issuedTo = issuedTo;
        this.issuedFrom = issuedFrom;
        this.fullName = fullName;
        this.signatureUrl = signatureUrl;
        this.createdAt = createAtTimeStamp;
        this.amount = amount;
    }

    public String getCheckId() {
        return id;
    }

    public String getSignatureUrl() {
        return signatureUrl;
    }

    public String getFullName() {
        return fullName;
    }

    public SecretUser getIssuedTo() {
        return issuedTo;
    }

    public SecretUser getIssuedFrom() {
        return issuedFrom;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getAmount() {
        return amount;
    }
}
