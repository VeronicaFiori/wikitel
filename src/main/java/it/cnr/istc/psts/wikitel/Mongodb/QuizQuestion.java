package it.cnr.istc.psts.wikitel.Mongodb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class QuizQuestion {

    private String id;
    private String question;
    private Map<String, String> options;
    @JsonProperty("correct_answer")
    private String correctAnswer;
    private String source;

}
