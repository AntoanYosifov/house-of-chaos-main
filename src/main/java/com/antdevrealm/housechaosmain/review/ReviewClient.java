package com.antdevrealm.housechaosmain.review;

import com.antdevrealm.housechaosmain.review.dto.ReviewResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "review-svc", url = "http://localhost:8081/api/v1/reviews")
public interface ReviewClient {
    @GetMapping("/{id}")
    ResponseEntity<ReviewResponseDTO> getReviewById(@PathVariable("id") UUID reviewId);
}
