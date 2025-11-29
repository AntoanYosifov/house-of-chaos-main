package com.antdevrealm.housechaosmain.review.dto;

import java.util.UUID;

public record ReviewResponseDTO(UUID id,
                                UUID authorId,
                                String authorName,
                                UUID subjectId,
                                String body) {
}
