package com.audition.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class Comment {

    int postId;
    int id;
    String name;
    String email;
    String body;
}
