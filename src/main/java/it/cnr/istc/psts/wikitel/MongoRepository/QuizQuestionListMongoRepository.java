package it.cnr.istc.psts.wikitel.MongoRepository;

import it.cnr.istc.psts.wikitel.Mongodb.QuizQuestionList;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface QuizQuestionListMongoRepository extends MongoRepository<QuizQuestionList, String> {

    boolean existsByRuleId(Long ruleId);

    Optional<QuizQuestionList> findByRuleId(Long ruleId);

}
