package com.audition.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuditionComment {

    private int postId;
    private int id;
    private String name;
    private String email;
    private String body;

}
