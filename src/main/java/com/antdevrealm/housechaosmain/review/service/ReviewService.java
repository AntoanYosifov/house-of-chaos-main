package com.antdevrealm.housechaosmain.review.service;

import com.antdevrealm.housechaosmain.exception.BusinessRuleException;
import com.antdevrealm.housechaosmain.exception.ResourceNotFoundException;
import com.antdevrealm.housechaosmain.review.client.ReviewClient;
import com.antdevrealm.housechaosmain.review.dto.CreateReviewRequestDTO;
import com.antdevrealm.housechaosmain.review.dto.ReviewResponseDTO;
import com.antdevrealm.housechaosmain.review.exception.ReviewServiceFeignCallException;
import com.antdevrealm.housechaosmain.user.model.UserEntity;
import com.antdevrealm.housechaosmain.user.repository.UserRepository;
import feign.FeignException;
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
    private final UserRepository userRepository;

    @Autowired
    public ReviewService(UserRepository userRepository, ReviewClient reviewClient) {
        this.userRepository = userRepository;
        this.reviewClient = reviewClient;
    }

    public List<ReviewResponseDTO> getAllByProductId(UUID productId) {
        try {
            ResponseEntity<List<ReviewResponseDTO>> httpResponse = this.reviewClient.getAllBySubjectId(productId);
            List<ReviewResponseDTO> body = httpResponse.getBody();
            int count = body != null ? body.size() : 0;
            log.info("Loaded {} reviews from review-svc for productId={}", count, productId);
            return body;
        } catch (FeignException e) {
            log.error("Error calling review-svc getAllBySubjectId for productId={}", productId, e);
            throw new ReviewServiceFeignCallException(
                    "Failed to load reviews due to a problem with the review service.", e);
        }
    }

    public ReviewResponseDTO createReview(CreateReviewRequestDTO dto) {
        UserEntity userEntity = this.userRepository.findById(dto.authorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("User with ID: %s not found", dto.authorId())));

        if (!dto.authorName().equals(userEntity.getFirstName())) {
            throw new BusinessRuleException(String.format(
                    "Author name: %s and username: %s do not match",
                    dto.authorName(), userEntity.getFirstName()));
        }

        try {
            ResponseEntity<ReviewResponseDTO> httpResponse = this.reviewClient.create(dto);
            ReviewResponseDTO body = httpResponse.getBody();

            if (body == null) {
                throw new ReviewServiceFeignCallException(
                        "Review service returned empty response when creating review.");
            }
            log.info("Review created via review-svc: reviewId={}, authorId={}, subjectId={}",
                    body.id(), body.authorId(), body.subjectId());
            return body;
        } catch (FeignException e) {
            log.error("Error calling review-svc create for authorId={}, subjectId={}",
                    dto.authorId(), dto.subjectId(), e);
            throw new ReviewServiceFeignCallException(
                    "Failed to create review due to a problem with the review service.", e);
        }
    }

    public void deleteById(UUID id, UUID userId) {
        try {
            ResponseEntity<ReviewResponseDTO> httpResponse = this.reviewClient.getById(id);
            ReviewResponseDTO body = httpResponse.getBody();

            if (body == null) {
                throw new ReviewServiceFeignCallException(
                        "Review service returned empty body when loading review " + id);
            }

            UUID authorId = body.authorId();
            if (!authorId.equals(userId)) {
                throw new BusinessRuleException(String.format(
                        "Author ID: %s and User ID: %s do not match", authorId, userId));
            }
            this.reviewClient.delete(id);
            log.info("Review deleted via review-svc: reviewId={}, userId={}", id, userId);

        } catch (FeignException e) {
            if (e.status() == 404) {
                throw new ResourceNotFoundException(
                        String.format("Review with ID: %s not found", id));
            }

            log.error("Error calling review-svc when deleting reviewId={}", id, e);
            throw new ReviewServiceFeignCallException(
                    "Failed to delete review due to a problem with the review service.", e);
        }
    }
}
