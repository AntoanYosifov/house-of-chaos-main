package com.antdevrealm.housechaosmain.review.web;

import com.antdevrealm.housechaosmain.review.dto.CreateReviewRequestDTO;
import com.antdevrealm.housechaosmain.review.dto.ReviewResponseDTO;
import com.antdevrealm.housechaosmain.review.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponseDTO> getById(@PathVariable UUID id) {
        ReviewResponseDTO responseDTO = this.reviewService.getReviewById(id);

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<List<ReviewResponseDTO>> getAllByProductId(@PathVariable("id") UUID productId) {
        List<ReviewResponseDTO> responseDTOS = this.reviewService.getAllByProductId(productId);
        return ResponseEntity.ok(responseDTOS);
    }

    @PostMapping
    public ResponseEntity<ReviewResponseDTO> create(@RequestBody @Valid CreateReviewRequestDTO dto) {
        ReviewResponseDTO responseDTO = this.reviewService.createReview(dto);

        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        this.reviewService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
