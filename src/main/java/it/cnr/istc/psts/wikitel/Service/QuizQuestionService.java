package it.cnr.istc.psts.wikitel.Service;

import it.cnr.istc.psts.wikitel.MongoRepository.QuizQuestionListMongoRepository;
import it.cnr.istc.psts.wikitel.Mongodb.QuizQuestion;
import it.cnr.istc.psts.wikitel.Mongodb.QuizQuestionList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuizQuestionService {

    @Autowired
    private QuizQuestionListMongoRepository quizQuestionListMongoRepository;

    public List<QuizQuestion> getQuizQuestionsByRule(Long ruleId) {
        QuizQuestionList quizQuestionListMongo = this.quizQuestionListMongoRepository.findById(ruleId).orElse(null);
        return quizQuestionListMongo != null ? quizQuestionListMongo.getQuizQuestions() : null;
    }

    public QuizQuestionList save(QuizQuestionList quizQuestionList) {
        return this.quizQuestionListMongoRepository.save(quizQuestionList);
    }

    public boolean existsQuizQuestionList(Long ruleId) {
        return quizQuestionListMongoRepository.existsByRuleId(ruleId);
    }
}
