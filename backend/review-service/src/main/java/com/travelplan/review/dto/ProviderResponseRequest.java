package com.travelplan.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for {@code POST /api/reviews/{id}/responses}.
 *
 * <p>A provider (hotel owner, tour guide, or vehicle owner) submits this
 * DTO to post a public reply to a tourist's review.  Only one response is
 * allowed per review per provider (enforced at the service layer).
 */
@Data
public class ProviderResponseRequest {

    /**
     * The response text.  Must not be blank and is capped at 2 000 characters
     * to prevent abuse.
     */
    @NotBlank(message = "Response content is required")
    @Size(max = 2000, message = "Response must not exceed 2000 characters")
    private String content;
}
