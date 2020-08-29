package com.iku.models;

import java.util.List;

public class feedbackImageModel {

    List<String> attachments;
    String subject, text;

    public feedbackImageModel() {

    }

    public feedbackImageModel(List<String> asList, String subject, String text) {
        this.attachments = asList;
        this.subject = subject;
        this.text = text;
    }


    public String getSubject() {
        return subject;
    }

    public String getText() {
        return text;
    }

    public List<String> getArrangements() {
        return attachments;
    }
}
