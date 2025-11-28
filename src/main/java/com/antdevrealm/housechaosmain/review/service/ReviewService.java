package com.antdevrealm.housechaosmain.review.service;

import com.antdevrealm.housechaosmain.review.client.ReviewClient;
import com.antdevrealm.housechaosmain.review.dto.CreateReviewRequestDTO;
import com.antdevrealm.housechaosmain.review.dto.ReviewResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ReviewService {
    private final ReviewClient reviewClient;

    @Autowired
    public ReviewService(ReviewClient reviewClient) {
        this.reviewClient = reviewClient;
    }

    public ReviewResponseDTO getReviewById(UUID id) {
        ResponseEntity<ReviewResponseDTO> httpResponse = reviewClient.getById(id);
        return httpResponse.getBody();
    }

    public List<ReviewResponseDTO> getAllByProductId(UUID productId) {
        ResponseEntity<List<ReviewResponseDTO>> httpResponse = this.reviewClient.getAllBySubjectId(productId);
        return httpResponse.getBody();
    }

    public ReviewResponseDTO createReview(CreateReviewRequestDTO dto) {
        ResponseEntity<ReviewResponseDTO> httpResponse = this.reviewClient.create(dto);
        return httpResponse.getBody();
    }

    public void deleteById(UUID id) {
        this.reviewClient.delete(id);
    }

}
