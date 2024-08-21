package it.cnr.istc.psts.wikitel.Mongodb;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document("QuizQuestionList")
public class QuizQuestionList {

    private Long ruleId;

    private List<QuizQuestion> quizQuestions;
}
