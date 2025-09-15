package com.creditScore.creditScore.controller;


import com.creditScore.creditScore.dto.CreditScoreDTO;
import com.creditScore.creditScore.dto.ScoreHistoryDTO;
import com.creditScore.creditScore.request.AddCreditScoreRequest;
import com.creditScore.creditScore.service.CreditScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/score")
public class CreditScoreController {

    @Autowired
    private CreditScoreService creditScoreService;

//    @Autowired
//    private AddCreditScoreRequest addCreditScoreRequest;


    @PostMapping("/add/{userId}")
    public CreditScoreDTO addCreditScore(@PathVariable int userId,
//                                         @RequestHeader("Authorization") String jwt,
                                         @RequestBody AddCreditScoreRequest addCreditScoreRequest) {
        System.out.print("Hello");
        return creditScoreService.addCreditScoreByUserId(userId, addCreditScoreRequest);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getCreditScore(@PathVariable int userId) {

        CreditScoreDTO creditScoreDTO = creditScoreService.getCreditScoreByEmailId(userId);
        if(creditScoreDTO == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Credit Score not found");
        }
        return ResponseEntity.ok(creditScoreDTO);
    }

    @PostMapping("/update/{userId}")
    public CreditScoreDTO updateCreditScore(@PathVariable int userId, @RequestBody AddCreditScoreRequest addCreditScoreRequest) {
        return creditScoreService.updateCreditScore(userId, addCreditScoreRequest);
    }

    @GetMapping("delete/{userId}")
    public void deleteCreditScore(@PathVariable int userId) {
        creditScoreService.deleteCreditScoreByUserId(userId);
    }

    @GetMapping("history/{userId}")
    public List<ScoreHistoryDTO> getCreditScoreHistoryByUserId(@PathVariable int userId)  {
        return creditScoreService.getCreditScoreHistoryByUserId(userId);
    }
}
