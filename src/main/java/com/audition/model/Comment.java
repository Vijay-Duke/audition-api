package com.audition.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
@Schema(description = "A comment on a post")
public class Comment {

    @Schema(description = "ID of the post this comment belongs to", example = "1")
    int postId;

    @Schema(description = "Unique identifier of the comment", example = "1")
    int id;

    @Schema(description = "Name/title of the comment", example = "id labore ex et quam laborum")
    String name;

    @Schema(description = "Email of the commenter", example = "Eliseo@gardner.biz")
    String email;

    @Schema(description = "Body content of the comment", example = "laudantium enim quasi...")
    String body;
}
