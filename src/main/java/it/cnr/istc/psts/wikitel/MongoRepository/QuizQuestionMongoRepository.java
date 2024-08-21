package it.cnr.istc.psts.wikitel.MongoRepository;

import it.cnr.istc.psts.wikitel.Mongodb.QuizQuestion;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface QuizQuestionMongoRepository extends MongoRepository<QuizQuestion, String> {
}
