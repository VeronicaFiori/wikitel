package it.cnr.istc.psts.wikitel.Service;

import it.cnr.istc.psts.wikitel.MongoRepository.RuleMongoRepository;
import it.cnr.istc.psts.wikitel.MongoRepository.SuggestionMRepository;
import it.cnr.istc.psts.wikitel.Mongodb.RuleMongo;
import it.cnr.istc.psts.wikitel.Mongodb.SuggestionM;
import it.cnr.istc.psts.wikitel.Repository.ModelRepository;
import it.cnr.istc.psts.wikitel.Repository.WikiSuggestionRepository;
import it.cnr.istc.psts.wikitel.controller.MainController;
import it.cnr.istc.psts.wikitel.db.LessonEntity;
import it.cnr.istc.psts.wikitel.db.ModelEntity;
import it.cnr.istc.psts.wikitel.db.UserEntity;
import it.cnr.istc.psts.wikitel.db.WikiSuggestionEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ModelService {

	@Autowired
	private ModelRepository modelRepository;
	
	 @Autowired
	 private WikiSuggestionRepository wikisuggestionrepository;
	 

	 
	 @Autowired
	 private RuleMongoRepository rulemongorep;
	 
	 @Autowired
	 private LessonService lessonservice;
	 
	 @Autowired
	 private RuleService ruleservice;
	 
	 @Autowired
	 private UserService userservice;
	 
	 @Autowired
	 private SuggestionMRepository suggestionm;
	
	 
		@Transactional
	    public SuggestionM saveSuggestionMongo(SuggestionM sm) {
	        return this.suggestionm.save(sm);
	    }
		
		 @Transactional
			public SuggestionM getSuggestion(String id) {
				Optional<SuggestionM> result  = this.suggestionm.findById(id);
				return result.orElse(null);
			}
	 
	 @Transactional
		public RuleMongo getRuleMongoByTitle(String id) {
			Optional<RuleMongo> result  = this.rulemongorep.findByTitle(id);
			return result.orElse(null);
		}

		public RuleMongo getRuleMongoById(Long id) {
			Optional<RuleMongo> result  = this.rulemongorep.findById(id);
			return result.orElse(null);
		}
	 
	 @Transactional
	 public List<RuleMongo> getmongotopics(String id) {
			List<RuleMongo> result  =  this.rulemongorep.findtopics(id);
			return result;
		}

		
	
	@Transactional
    public ModelEntity save(ModelEntity model) {
        return this.modelRepository.save(model);
    }
	
	@Transactional
    public void delete(Long id, UserEntity user) {
		ModelEntity m = this.getModel(id);
		
		m.getTeachers().stream().forEach(t -> t.getModels().remove(m)); 
		List<LessonEntity> les = new ArrayList<>();
		for(LessonEntity l : this.lessonservice.getlessonbymodel(m)) {
				les.add(l);
				l.setModel(null);
			for(UserEntity u : l.getFollowed_by()) {	

				File lesson_file = new File(System.getProperty("user.dir")+"//riddle//" + l.getId() + u.getId() + ".rddl");
				lesson_file.delete();

			String string = String.valueOf(l.getId()) + String.valueOf(u.getId());
			MainController.LESSONS.remove(string);
			}
				
			}
		for(LessonEntity d : les) {
			this.lessonservice.delete(d);
		}
		
	
         this.modelRepository.deleteById(id);
    }
	
	
	@Transactional
	public List<ModelEntity> getModelTeacher(UserEntity teacher) {
		List<ModelEntity> result = this.modelRepository.findByTeachers(teacher);
		return result;
	}
	
	@Transactional
	public ModelEntity getModel(Long id) {
		Optional<ModelEntity> result  = this.modelRepository.findById(id);
		return result.orElse(null);
	}
	
	@Transactional
	public WikiSuggestionEntity getpage(String page) {
		Optional<WikiSuggestionEntity> result = this.wikisuggestionrepository.findByPage(page);
		return result.orElse(null);
	}
	
	@Transactional
    public WikiSuggestionEntity savewikisuggestion(WikiSuggestionEntity wikisugg) {
        return this.wikisuggestionrepository.save(wikisugg);
    }
	

}
