package com.creditScore.creditScore.service;

import com.creditScore.creditScore.client.UserManagementClient;
import com.creditScore.creditScore.dto.CreditScoreDTO;
import com.creditScore.creditScore.dto.NotificationDTO;
import com.creditScore.creditScore.dto.ScoreDTO;
import com.creditScore.creditScore.dto.ScoreHistoryDTO;
import com.creditScore.creditScore.config.RedisConfig;
import com.creditScore.creditScore.entity.CreditScore;
import com.creditScore.creditScore.repositoty.CreditScoreRepository;
import com.creditScore.creditScore.request.AddCreditScoreRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

@Service
public class CreditScoreService {

    @Autowired
    private CreditScoreRepository creditScoreRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserManagementClient userManagementClient;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    //Create logger for this class
    private static final Logger logger = LogManager.getLogger(CreditScoreService.class);
    private static final String CREDIT_SCORE_UPDATE = "credit-score-update";

    // Method to extract email from JSON string
    public static String extractEmailFromJson(String jsonString) {
        try {
            // Create ObjectMapper instance to parse JSON
            ObjectMapper objectMapper = new ObjectMapper();

            // Parse JSON string into JsonNode
            JsonNode jsonNode = objectMapper.readTree(jsonString);

            // Extract the "email" field from the JSON
            return jsonNode.get("email").asText();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public CreditScoreDTO addCreditScoreByUserId(int userId, AddCreditScoreRequest addCreditScoreRequest) {
        CreditScore creditScore = new CreditScore();
        creditScore.setUserId(userId);
        creditScore.setScore(addCreditScoreRequest.getScore());
        creditScore.setDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        creditScore.setLast_updated(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        creditScoreRepository.save(creditScore);

        String userDetails = userManagementClient.getuserDetails(userId).block();  //Synchronous call to get user details
        String emailId = extractEmailFromJson(userDetails);
        System.out.print(userDetails);
        System.out.print(emailId);

        if (emailId != null && !emailId.isEmpty())
            redisTemplate.opsForValue().set(emailId, creditScore);
        else
            System.out.print("Empty email address");
        return convertToDTO(creditScore);
    }

    public CreditScoreDTO getCreditScoreByEmailId(int userId) {
        //Synchronous call to get user details
        String userDetails = userManagementClient.getuserDetails(userId).block();  //Synchronous call to get user details
        String emailId = extractEmailFromJson(userDetails);
        System.out.print(userDetails);
        System.out.print(emailId);
        if(emailId == null){
            logger.info("Unable to find emailId");
        }
        CreditScore creditScore = (CreditScore) redisTemplate.opsForValue().get(emailId); //Synchronus call to gte user details
        //If credit score not in redis cache
        if(creditScore == null){
            logger.info("Fetching from DB");
            creditScore = creditScoreRepository.findTopByUserIdOrderByDateDesc(userId);
        } else {
            logger.info("Fetching from redis");
        }
        sendToKafka(creditScore.getScore(), emailId);
        return convertToDTO(creditScore);
    }

    //Send credit score update to kafka topic
    private void sendToKafka(int creditScore, String emailId) {
        NotificationDTO message = new NotificationDTO(creditScore, emailId);
        kafkaTemplate.send(CREDIT_SCORE_UPDATE, message);
    }

    //get credit score for user id from database
    public CreditScoreDTO getCreditScoreByUserId(int userId) {
        String emailId = userManagementClient.getuserDetails(userId).block();  //Synchronous call to get user details
        CreditScore creditScore = creditScoreRepository.findTopByUserIdOrderByDateDesc(userId);
        return convertToDTO(creditScore);
    }

    //update credit score in database
    public CreditScoreDTO updateCreditScore(int userId, AddCreditScoreRequest addCreditScoreRequest) {
        CreditScore existingScore = creditScoreRepository.findTopByUserIdOrderByDateDesc(userId);
        if(existingScore == null){
            throw new IllegalArgumentException("Credit score not found");
        }
        existingScore.setScore(addCreditScoreRequest.getScore());
        existingScore.setDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        existingScore = creditScoreRepository.save(existingScore);
        return convertToDTO(existingScore);
    }

    //Delete credit score by user id
    public void deleteCreditScoreByUserId(int userId) {
        CreditScore latestCreditScore = creditScoreRepository.findTopByUserIdOrderByDateDesc(userId);
        creditScoreRepository.delete(latestCreditScore);
    }

    //Retrieve credit score history for a user transforming each score into detailed DTO
    public List<ScoreHistoryDTO> getCreditScoreHistoryByUserId(int userId) {
        List<CreditScore> scoreHistory = creditScoreRepository.findByUserId(userId);
        System.out.print(scoreHistory);
        return scoreHistory.stream()
                .collect(Collectors.groupingBy(CreditScore::getUserId))  // Group by userId
                .entrySet().stream()
                .map(entry -> new ScoreHistoryDTO(entry.getKey(),
                        entry.getValue().stream()
                                .map(this::convertToScoreDTO)
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    //Convert credit score to score dto
    public ScoreDTO convertToScoreDTO(CreditScore creditScore) {
        ScoreDTO scoreDTO = new ScoreDTO();
        scoreDTO.setScore(creditScore.getScore());
        scoreDTO.setDate(creditScore.getDate());
        return scoreDTO;
    }

//    //Calculate new credit score and save it to database and cache
//    public CreditScoreDTO calculateCreditScore(CreditScoreDTO creditScoreDTO) {
//        String emailId = userManagementClient.getuserDetails(creditScoreDTO.getUserId()).block();
//        redisTemplate.opsForValue().set(emailId, convertToEntity(creditScoreDTO));
//        CreditScore creditScore = CreditScoreRepository.save(convertToEntity(creditScoreDTO));
//        return convertToDTO(creditScore);
//    }

    //Convert creditScore entity to scoreHistoryDTO
//    private ScoreHistoryDTO convertToScoreHistoryDTO(CreditScore creditScore) {
//        ScoreHistoryDTO scoreHistoryDTO = new ScoreHistoryDTO();
//        scoreHistoryDTO.setUserId(creditScore.getUserId());
//        ScoreHistoryDTO.CreditScoreDetails details = new ScoreHistoryDTO.CreditScoreDetails();
//        details.setScore(creditScore.getScore());
//        details.setDate(creditScore.getDate());
//        scoreHistoryDTO.setScores(List.of(details));
//        return scoreHistoryDTO;
//    }

    //Convert CreditScore entity to CreditScore DTO
    private CreditScoreDTO convertToDTO(CreditScore creditScore) {
        CreditScoreDTO creditScoreDTO = new CreditScoreDTO();
        creditScoreDTO.setUserId(creditScore.getUserId());
        creditScoreDTO.setScore(creditScore.getScore());
        creditScoreDTO.setDate(creditScore.getDate());
        creditScoreDTO.setLast_updated(creditScore.getLast_updated());
        return creditScoreDTO;
    }

    //Convert CreditScore DTO to CreditScore entity
    private CreditScore convertToEntity(CreditScoreDTO creditScoreDTO) {
       CreditScore creditScore = new CreditScore();
        creditScore.setId(creditScoreDTO.getUserId());
        creditScore.setScore(creditScoreDTO.getScore());
        creditScore.setDate(creditScoreDTO.getDate());
        creditScore.setLast_updated(creditScoreDTO.getLast_updated());
        return creditScore;
    }
}
