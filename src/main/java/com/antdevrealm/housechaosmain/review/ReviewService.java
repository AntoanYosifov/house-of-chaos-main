package com.antdevrealm.housechaosmain.review;

import com.antdevrealm.housechaosmain.review.dto.ReviewResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class ReviewService {
    private final ReviewClient reviewClient;

    @Autowired
    public ReviewService(ReviewClient reviewClient) {
        this.reviewClient = reviewClient;
    }

    public ReviewResponseDTO getReviewById() {
        UUID uuid = UUID.fromString("fdd6ae1d-d697-4e57-a918-c120df271745");
        ResponseEntity<ReviewResponseDTO> reviewById = reviewClient.getReviewById(uuid);
        ReviewResponseDTO body = reviewById.getBody();
        log.info("Returned body {}", body);
        return body;
    }

}
