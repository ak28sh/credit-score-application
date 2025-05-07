package com.creditScore.creditScore.repositoty;

import com.creditScore.creditScore.entity.CreditScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CreditScoreRepository extends JpaRepository<CreditScore, Integer> {
    //find all credit score associated with user Id
    List<CreditScore> findByUserId(int userId);

    //delete all credit score for user
    void deleteByUserId(int userId);

    //Find most recent credit score
    CreditScore findTopByUserIdOrderByDateDesc(int userId);

    //find most recent credit score given email id of user
//    CreditScore findTopByEmailIdOrderByDateDesc(String emailId);

//    List<CreditScore> findByLastUpdatedBefore(String dateTime);
}
