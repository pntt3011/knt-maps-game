package com.example.maplogin;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class QuestionInfo implements Info{
    public String content;
    public String imageUrl;

    public String choice1;
    public String choice2;
    public String choice3;
    public String choice4;

    public Long answer;

    public QuestionInfo() {}

    public QuestionInfo(String content,
                        String imageUrl,
                        String choice1,
                        String choice2,
                        String choice3,
                        String choice4,
                        Long answer) {
        this.content = content;
        this.imageUrl = imageUrl;
        this.choice1 = choice1;
        this.choice2 = choice2;
        this.choice3 = choice3;
        this.choice4 = choice4;
        this.answer = answer;
    }
}