package it.cnr.istc.psts.wikitel.db;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class QuizQuestion {

    private String question;
    private Map<String, String> options;
    @JsonProperty("correct_answer")
    private String correctAnswer;

}
