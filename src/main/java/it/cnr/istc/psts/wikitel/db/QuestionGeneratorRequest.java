package it.cnr.istc.psts.wikitel.db;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class QuestionGeneratorRequest {

    private String model;

    private double temperature;

    private List<Messages> messages;


    public QuestionGeneratorRequest() {
        this.messages = new ArrayList<>();
    }
}
