package com.creditScore.creditScore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreHistoryDTO {

    private int userId;
    private List<ScoreDTO> scores;

//    @Data
//    @NoArgsConstructor
//    @AllArgsConstructor
//    public static class CreditScoreDetails {
//        private int score;
//        private String date;
//    }
}
