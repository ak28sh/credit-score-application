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
import org.springframework.scheduling.annotation.Async;
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

    @Autowired
    private EmailService emailService;

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

    public String getEmailFromUserId(int userId) {
        //Synchronous call to get user details
        String userDetails = userManagementClient.getuserDetails(userId).block();  //Synchronous call to get user details
        System.out.print(userDetails);
        if (userDetails == null || userDetails.isEmpty()) {
            logger.info("User details not found for userId: {}", userId);
            return null;
        }
        return extractEmailFromJson(userDetails);
    }



    /*
    * Function takes userId and AddCreditScoreRequest as input
    * Create a new CreditScore entity and set its properties
    * Save the CreditScore entity to the database using creditScoreRepository

    * Retrieve user details using userManagementClient
    * Extract email from user details JSON
    * Store the CreditScore entity in Redis cache with email as key
    * Return the saved CreditScore entity converted to CreditScoreDTO
    */
    public CreditScoreDTO addCreditScoreByUserId(int userId, AddCreditScoreRequest addCreditScoreRequest) {
        CreditScore creditScore = new CreditScore();
        creditScore.setUserId(userId);
        creditScore.setScore(addCreditScoreRequest.getScore());
        creditScore.setDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        creditScore.setLast_updated(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        creditScoreRepository.save(creditScore);

        String emailId = getEmailFromUserId(userId);

        if (emailId != null && !emailId.isEmpty()) {
            logger.info("Retrieved emailId: {} for userId: {}", emailId, userId);
            redisTemplate.opsForValue().set(emailId, creditScore);
            logger.info("Credit score cached in Redis for emailId: {}", emailId);
            emailService.sendEmail(emailId, "Credit Score Added", "Dear Customer, Your new credit score is: " + creditScore.getScore());
        }
        else
            System.out.print("Empty email address");
        return convertToDTO(creditScore);
    }

    /*
    * Function takes userId as input
    * Get user details using userManagementClient
    * Extract email from user details JSON
    * Check Redis cache for CreditScore using email as keys
    * If not found in cache, retrieve from database
    * Send credit score update to Kafka topic
    * Return the CreditScore entity converted to CreditScoreDTO
    */

    @Async("customExecutor")
    public CreditScoreDTO getCreditScoreByEmailId(int userId) {

        System.out.println("Processing credit score request for user id " + userId + " in thread " + Thread.currentThread().getName());

        //Synchronous call to get user details
        String userDetails = userManagementClient.getuserDetails(userId).block();  //Synchronous call to get user details
        System.out.print(userDetails);
        if (userDetails == null || userDetails.isEmpty()) {
            logger.info("User details not found for userId: {}", userId);
            return null;
        }

        String emailId = extractEmailFromJson(userDetails);
        System.out.print(emailId);
        if (emailId == null || emailId.isEmpty()) {
            logger.info("Email ID not found in user details for userId: {}", userId);
            return null;
        }

        CreditScore creditScore = (CreditScore) redisTemplate.opsForValue().get(emailId); //Synchronus call to gte user details
        //If credit score not in redis cache
        if(creditScore == null){
            logger.info("Credit score not found in Redis for emailId: {}. Fetching from DB.", emailId);
            creditScore = creditScoreRepository.findTopByUserIdOrderByDateDesc(userId);
            if(creditScore == null){
                logger.info("Credit score not found in DB for userId: {}", userId);
                return null;
            }
        } else {
            logger.info("Credit score retrieved from Redis for emailId: {}", emailId);
        }
        sendToKafka(creditScore.getScore(), emailId);
        return convertToDTO(creditScore);
    }

    /*
    Function takes creditScore and emailId as input
    Create a NotificationDTO message with creditScore and emailId
    Send the message to Kafka topic CREDIT_SCORE_UPDATE using kafkaTemplate
    * */

    //Send credit score update to kafka topic
    private void sendToKafka(int creditScore, String emailId) {
        NotificationDTO message = new NotificationDTO(creditScore, emailId);
        kafkaTemplate.send(CREDIT_SCORE_UPDATE, message);
    }

    /*
    * Function takes userId as input
    * Find lastest credit score for userId from database
    * Convert the CreditScore entity to CreditScoreDTO
    * Return the CreditScoreDTO
    * */

    //get credit score for user id from database
    public CreditScoreDTO getCreditScoreByUserId(int userId) {
        //String emailId = userManagementClient.getuserDetails(userId).block();  //Synchronous call to get user details
        CreditScore creditScore = creditScoreRepository.findTopByUserIdOrderByDateDesc(userId);
        return convertToDTO(creditScore);
    }

    /*
    * Function takes userId and AddCreditScoreRequest as input
    * Find latest credit score for userId from database
    * Update the score and date properties of the CreditScore entity
    * Save the updated CreditScore entity to the database
    * Convert the updated CreditScore entity to CreditScoreDTO
    * Return the CreditScoreDTO
    * */

    //update credit score in database
    public CreditScoreDTO updateCreditScore(int userId, AddCreditScoreRequest addCreditScoreRequest) {
        CreditScore existingScore = creditScoreRepository.findTopByUserIdOrderByDateDesc(userId);
        if(existingScore == null){
            throw new IllegalArgumentException("Credit score not found");
        }
        existingScore.setScore(addCreditScoreRequest.getScore());
        existingScore.setDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        existingScore = creditScoreRepository.save(existingScore);
        String emailId = getEmailFromUserId(userId);
        redisTemplate.opsForValue().set(emailId, existingScore);
        return convertToDTO(existingScore);
    }

    /*
    * Function takes userId as input
    * Find latest credit score for userId from database
    * Delete the CreditScore entity from the database
    * Return void
    * */

    //Delete credit score by user id
    public void deleteCreditScoreByUserId(int userId) {
        CreditScore latestCreditScore = creditScoreRepository.findTopByUserIdOrderByDateDesc(userId);
        creditScoreRepository.delete(latestCreditScore);
    }

    /*
    * Function takes userId as input
    * Find all credit scores for userId from database
    * Group the CreditScore entities by userId
    * Transform each CreditScore entity to ScoreDTO using convertToScoreDTO method
    * Create a ScoreHistoryDTO for each userId with the list of ScoreDTOs
    * Collect all ScoreHistoryDTOs into a list and return it
    * */

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
