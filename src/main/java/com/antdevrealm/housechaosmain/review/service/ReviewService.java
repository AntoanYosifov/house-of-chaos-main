package com.antdevrealm.housechaosmain.review.service;

import com.antdevrealm.housechaosmain.exception.BusinessRuleException;
import com.antdevrealm.housechaosmain.exception.ResourceNotFoundException;
import com.antdevrealm.housechaosmain.review.client.ReviewClient;
import com.antdevrealm.housechaosmain.review.dto.CreateReviewRequestDTO;
import com.antdevrealm.housechaosmain.review.dto.ReviewResponseDTO;
import com.antdevrealm.housechaosmain.user.model.UserEntity;
import com.antdevrealm.housechaosmain.user.repository.UserRepository;
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

    public ReviewResponseDTO getReviewById(UUID id) {
        ResponseEntity<ReviewResponseDTO> httpResponse = reviewClient.getById(id);
        return httpResponse.getBody();
    }

    public List<ReviewResponseDTO> getAllByProductId(UUID productId) {
        ResponseEntity<List<ReviewResponseDTO>> httpResponse = this.reviewClient.getAllBySubjectId(productId);
        return httpResponse.getBody();
    }

    public ReviewResponseDTO createReview(CreateReviewRequestDTO dto) {
        UserEntity userEntity = this.userRepository.findById(dto.authorId())
                .orElseThrow(() -> new ResourceNotFoundException(String.format("User with ID: %s not found", dto.authorId())));

        if(!dto.authorName().equals(userEntity.getFirstName())) {
            throw new BusinessRuleException(String.format("Author name: %s and username: %s do not match", dto.authorName(), userEntity.getFirstName()));
        }

        ResponseEntity<ReviewResponseDTO> httpResponse = this.reviewClient.create(dto);
        return httpResponse.getBody();
    }

    public void deleteById(UUID id) {
        this.reviewClient.delete(id);
    }

}
