package com.antdevrealm.housechaosmain.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateReviewRequestDTO(
        @NotNull(message = "User ID is required")
        UUID authorId,

        @NotBlank(message = "Author name is required")
        String authorName,

        @NotNull(message = "Product ID is required")
        UUID subjectId,

        @NotBlank(message = "Review body is required")
        String body
) {}
