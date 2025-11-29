package com.antdevrealm.housechaosmain.review.client;

import com.antdevrealm.housechaosmain.review.dto.CreateReviewRequestDTO;
import com.antdevrealm.housechaosmain.review.dto.ReviewResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "review-svc", url = "http://localhost:8081/api/v1/reviews")
public interface ReviewClient {
    @GetMapping("/{id}")
    ResponseEntity<ReviewResponseDTO> getById(@PathVariable("id") UUID reviewId);

    @GetMapping("/subject/{id}")
    ResponseEntity<List<ReviewResponseDTO>> getAllBySubjectId(@PathVariable("id") UUID subjectId);

    @PostMapping
    ResponseEntity<ReviewResponseDTO> create(@RequestBody CreateReviewRequestDTO dto);

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable UUID id);
}
