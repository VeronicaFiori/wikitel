package it.cnr.istc.psts.wikitel.Mongodb;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
@Document("QuizQuestionList")
public class QuizQuestionList {
    @Field("_Id")
    private Long ruleId;

    private List<QuizQuestion> quizQuestions;
}
