package com.audition.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
@Schema(description = "A post from the audition API")
public class AuditionPost {

    @Schema(description = "ID of the user who created the post", example = "1")
    Long userId;

    @Schema(description = "Unique identifier of the post", example = "1")
    Long id;

    @Schema(description = "Title of the post", example = "sunt aut facere repellat provident")
    String title;

    @Schema(description = "Body content of the post", example = "quia et suscipit...")
    String body;

    @Schema(description = "Comments on this post (only included when include=comments)")
    List<Comment> comments;
}
