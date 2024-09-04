package it.cnr.istc.psts.wikitel.controller;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.cnr.istc.psts.Websocket.Sending;
import it.cnr.istc.psts.wikitel.Service.ModelService;
import it.cnr.istc.psts.wikitel.Service.RuleService;
import it.cnr.istc.psts.wikitel.Service.Starter;
import it.cnr.istc.psts.wikitel.Service.UserService;
import it.cnr.istc.psts.wikitel.db.FileRuleEntity;
import it.cnr.istc.psts.wikitel.db.LessonEntity;
import it.cnr.istc.psts.wikitel.db.RuleEntity;
import it.cnr.istc.psts.wikitel.db.TextRuleEntity;
import it.cnr.istc.psts.wikitel.db.UserEntity;
import it.cnr.istc.psts.wikitel.db.WebRuleEntity;
import it.cnr.istc.psts.wikitel.db.WikiRuleEntity;
import it.cnr.psts.wikitel.API.Lesson.LessonState;
import it.cnr.psts.wikitel.API.Message;
import it.cnr.psts.wikitel.API.Message.Stimulus;
import it.cnr.psts.wikitel.API.Timeline;
import it.cnr.psts.wikitel.API.TimelineValue;



public class LessonManager  {

	ModelService modelservice;

	RuleService ruleservice;

	UserService userservice;

	private Sending send;


	static final Logger LOG = LoggerFactory.getLogger(LessonManager.class);
	public  LessonEntity lesson;
	private ScheduledFuture<?> scheduled_feature;
	//private final Solver solver = new Solver();
	
    private Executor executor;

	//private final TimelinesExecutor executor = new TimelinesExecutor(solver ,"{}", new Rational(1));
	
	private final Set<String> topics = new HashSet<>();
	private LessonState state = LessonState.Stopped;
	private final Map<Long, Collection<Stimulus>> stimuli = new HashMap<>();
	private UserEntity student;
	private Timeline timeline = new Timeline(new ArrayList<>()); 




	Stimulus st = null;



	public LessonManager(final LessonEntity lesson, final Sending send, final ModelService modelservice, final UserService userservice,final RuleService ruleservice ) {
		this.lesson = lesson;
		this.send=send;
		this.modelservice = modelservice;
		this.ruleservice = ruleservice;
		this.userservice = userservice;
		for (final UserEntity student : lesson.getFollowed_by()) {
			stimuli.put(student.getId(), new ArrayList<>());
			this.student = student;
		}

	}
	
	
	public List<RuleEntity> getArgomentiPerStudenti() throws JsonMappingException, JsonProcessingException  {
		final List<RuleEntity> argomenti= new ArrayList<>();
		
		ObjectMapper mapper = new ObjectMapper();
			List<String> profile = mapper.readValue(student.getProfile(), new TypeReference<List<String>>(){});
			for (RuleEntity arg : lesson.getGoals()) {
				if(arg instanceof WebRuleEntity || arg instanceof TextRuleEntity || arg instanceof FileRuleEntity){
					argomenti.add(arg);
				}else{
					for(String topic: arg.getTopics()) {
						if (profile.contains(topic)) {
							argomenti.add(arg);
							break;
						}
					}
				}
			}
		return argomenti;
	}

	
	


	public void Solve() throws JsonMappingException, JsonProcessingException {
		final StringBuilder sb = new StringBuilder();
		to_string(sb, lesson);
		List<RuleEntity> argomentiPerStudenti = getArgomentiPerStudenti();
		if (!argomentiPerStudenti.isEmpty()) {
          System.out.println("At least one argument is present in the user's interests.");
			setTimeline(argomentiPerStudenti);
      } else {
          System.out.println("No arguments are present in the user's interests.");
      }
      
	}
	


	
	

	public void play() {
		scheduled_feature.cancel(false);
		setState(LessonState.Running);
    }
	

	

	




//	public void play() {
//		System.out.println("User lesson MANAGER:"+ student.getId());
//		scheduled_feature = Starter.EXECUTOR.scheduleAtFixedRate(() -> {
//			try {
//				executor.tick();
//			} catch (ExecutorException e) {
//				LOG.error("cannot execute the given solution..", e);
//				scheduled_feature.cancel(false);
//			}
//			final FileWriter writer = new FileWriter(lesson_file, false);
//			writer.append(sb);
//			writer.close();
//		} catch (final IOException e) {
//			LOG.error("Cannot create lesson problem file", e);
//		}
//		}, 1, 1, TimeUnit.SECONDS);
//		setState(LessonState.Running);
//	}
//
//		LOG.info("Reading lesson \"{}\" planning problem..", lesson.getName());
//		try {// we load the planning problem..
//			solver.read(sb.toString());
//		} catch (SolverException e) {
//			LOG.error("cannot read the given problem..", e);
//		}
//		LOG.info("Solving lesson \"{}\" planning problem..", lesson.getName());
//		try { // we solve the planning problem..
//			solver.solve();
//		} catch (SolverException e) {
//			LOG.error("cannot solve the given problem..", e);
//		}
		
		
	public void pause() {
		scheduled_feature.cancel(false);
		setState(LessonState.Paused);
	}

		
		
	public void stop() {
		scheduled_feature.cancel(false);
		setState(LessonState.Stopped);
	}
	
	
	


	public void setTimeline(List<RuleEntity> argomentiPerStudenti){
	//	this.timeline = new Timeline(new ArrayList<>());
		float horizon = 0;
		float currentTime = 0;
		for(RuleEntity arg : argomentiPerStudenti){
			horizon += arg.getLength();
			TimelineValue timelineValue = new TimelineValue();
			timelineValue.setArg(arg.getName());
			timelineValue.setHorizon(arg.getLength());
			timelineValue.setFrom(currentTime);
			timelineValue.setTo(currentTime + arg.getLength());
			this.timeline.getValue().add(timelineValue);
			currentTime += arg.getLength();
		}
		this.timeline.setHorizon(horizon);
			
	}
	
	public Timeline geTimeline(){
		return this.timeline;
	}
	

	public LessonState getState() {
		return state;
	}


	private void setState(final LessonState state) {
		this.state = state;
		try {
			final String wsc = UserController.ONLINE.get(lesson.getTeacher().getId());

			if (wsc != null) {
				send.notify(Starter.mapper.writeValueAsString(new Message.LessonStateUpdate(lesson.getId(), state)), wsc);
			}
			for (final UserEntity student : lesson.getFollowed_by()) {
				final String student_wsc = UserController.ONLINE.get(student.getId());
				if (student_wsc != null) {
					send.notify(Starter.mapper.writeValueAsString(new Message.LessonStateUpdate(lesson.getId(), state)), student_wsc);
				}
			}
		} catch (final JsonProcessingException e) {
			LOG.error("cannot notify lesson state update..", e);
		}
	}

	/**
	 * @return the stimuli
	 */
	public Collection<Stimulus> getStimuli(final long user_id) {
		if (!stimuli.containsKey(user_id) || stimuli.get(user_id).isEmpty())
			return null;
		return stimuli.get(user_id);
	}

	public LessonEntity getlesson() {
		return lesson;
	}

	private static void to_string(final StringBuilder sb, final LessonEntity lesson_entity) {
		to_string(lesson_entity, sb );


		sb.append("\n\n");
		sb.append("Lesson l_").append(lesson_entity.getId()).append(" = new Lesson();\n");
		for (final UserEntity student : lesson_entity.getFollowed_by()) {
			sb.append("User u_").append(student.getId()).append(" = new User(").append(student.getId());
			try {
				ObjectMapper mapper = new ObjectMapper();
				List<String> profile = mapper.readValue(student.getProfile(), new TypeReference<List<String>>(){});
				Json_reader interests = pageController.json("/json/user_model.json",true);
				for (Interests interest : interests.getInterests()) {
					Boolean i=false;
					if(profile.contains(interest.getId())) {
						i=true;
					}
					sb.append(", ").append(i);

				}
				sb.append(");\n");
			} catch (final JsonProcessingException e) {
				LOG.error("Cannot parse profile", e);
			}
		}

		sb.append("\nUser u;\n");
		for (final RuleEntity goal : lesson_entity.getGoals()) {
			sb.append("\n{\n");
			sb.append("  goal st").append(goal.getId()).append(" = new l_").append(lesson_entity.getId()).append(".St_")
					.append(goal.getId()).append("(u:u);\n");
			if(!goal.getTopics().isEmpty()) {
				sb.append("} or {\n");
				for (String topic : goal.getTopics())
					sb.append("  !u.").append(to_id(topic)).append(";\n");
				sb.append("}\n");
			}else {
				sb.append("    }");
			}

		}
	}

	private static void to_string(final LessonEntity lesson_entity, final StringBuilder sb) {
		sb.append("class User {\n\n");
		sb.append("  int id;\n");
		for (final JsonNode interest : Starter.USER_MODEL.get("interests"))
			sb.append("  bool ").append(to_id(interest.get("id").asText())).append(";\n");
		sb.append("  ReusableResource busy_time = new ReusableResource(1.0);\n\n");
		sb.append("  User(int id");
		for (final JsonNode interest : Starter.USER_MODEL.get("interests"))
			sb.append(", bool ").append(to_id(interest.get("id").asText()));
		sb.append(") : id(id)");
		for (final JsonNode interest : Starter.USER_MODEL.get("interests"))
			sb.append(", ").append(to_id(interest.get("id").asText())).append('(')
					.append(to_id(interest.get("id").asText())).append(')');
		sb.append(" {}\n");
		sb.append("}\n\n");

		sb.append("class Lesson {\n");
		//model_entity.getRules().forEach(rule -> to_string(sb, rule));
		lesson_entity.getGoals().forEach(goal -> to_string(sb, goal));
		sb.append("}");
	}

	private static void to_string(final StringBuilder sb, final RuleEntity rule_entity) {
		sb.append("\n  predicate ").append("St_").append(rule_entity.getId()).append("(User u) : Interval {\n");
		sb.append("    duration >= ").append(rule_entity.getLength()).append(".0;\n");
		sb.append("    fact bt = new u.busy_time.Use(start:start, duration:duration, end:end, amount:1.0);\n");
		sb.append("  }\n");
	}

	private static String to_id(final String c_id) {
		return Normalizer.normalize(c_id.replace("Categoria:", "c_"), Normalizer.Form.NFD).replaceAll("%27|\\p{M}", "")
				.toLowerCase();
	}

	public static MessageHeaders createHeaders(String sessionId) {
		SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
		headerAccessor.setSessionId(sessionId);
		headerAccessor.setLeaveMutable(true);
		return headerAccessor.getMessageHeaders();
	}


	/**
	 * @return the topics
	 */
	public Set<String> getTopics() {
		return Collections.unmodifiableSet(topics);
	}


}