package com.antdevrealm.housechaosmain.review.dto;

import java.util.UUID;

public record ReviewResponseDTO(UUID id,
                                UUID authorId,
                                UUID subjectId,
                                String body) {
}
