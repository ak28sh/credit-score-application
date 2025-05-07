package com.creditScore.creditScore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinancialDataDTO {

    private int userId;
    private double creditScore;
    private double transactionAmount;
    private String emailId;
    private String transactionType;

}
