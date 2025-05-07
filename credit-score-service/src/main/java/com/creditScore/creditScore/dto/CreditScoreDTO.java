package com.creditScore.creditScore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreditScoreDTO {
    private int userId;
    private int score;
    private String date;
    private String last_updated;
}
