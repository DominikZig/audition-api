package com.audition.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuditionPost {

    private int userId;
    private int id;
    private String title;
    private String body;

    @Setter
    private List<AuditionComment> comments;

}
