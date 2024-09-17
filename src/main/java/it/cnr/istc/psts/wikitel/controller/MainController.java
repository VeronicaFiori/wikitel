package it.cnr.istc.psts.wikitel.controller;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.cnr.istc.psts.Websocket.Sending;
import it.cnr.istc.psts.wikitel.MongoRepository.RuleMongoRepository;
import it.cnr.istc.psts.wikitel.Mongodb.*;
import it.cnr.istc.psts.wikitel.Repository.ModelRepository;
import it.cnr.istc.psts.wikitel.Repository.Response;
import it.cnr.istc.psts.wikitel.Repository.RuleRepository;
import it.cnr.istc.psts.wikitel.Repository.UserRepository;
import it.cnr.istc.psts.wikitel.Service.*;
import it.cnr.istc.psts.wikitel.db.*;
import it.cnr.psts.wikitel.API.Lesson.LessonState;
import it.cnr.psts.wikitel.API.Message;
import net.bytebuddy.utility.RandomString;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

;

@RestController
public class MainController {

  static final Logger LOG = LoggerFactory.getLogger(MainController.class);

  @Autowired
  protected PasswordEncoder passwordEncoder;

  @Autowired
  private UserRepository userrepository;
  
  @Autowired
  private RuleRepository rulerep;

  @Autowired
  private RuleMongoRepository rulemongorep;
  
  @Autowired
  private RuleService ruleservice;

  @Autowired
  private UserService userservice;

  @Autowired
  private CredentialService credentialservice;

  @Autowired
  private LessonService lessonservice;

  @Autowired
  private FilesService fileservice;

  @Autowired
  private ModelService modelservice;
  @Autowired
  private ModelRepository modelrepository;

  @Autowired
  private Sending send;

  @Autowired
  private RuleSuggestionRelationService relationservice;

  private LessonEntity l;

  private ModelEntity m;

  private Long Fileid;
  
  public Map<Long, ArrayList<String>> ricerca = new HashMap<>();
  
  public static final List<Long> newUsers = new ArrayList<>();

  @Value("${url.chatbot}")
  private String chatBotUrl;

  @Value("${url.chatbot.apikey}")
  private String chatBotKey;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private QuizQuestionService quizQuestionService;
  public static final Map < String, LessonManager > LESSONS = new HashMap < > ();

  @PostMapping("/register")
  public Response register(@RequestBody ObjectNode node,HttpServletRequest request) throws JsonGenerationException, JsonMappingException, IOException, MessagingException {
    Json_reader interests = new Json_reader();
    Response response = new Response("Done");
    UserEntity nuovo = new UserEntity();
    Credentials newCred = new Credentials();
    newCred.setEmail(node.get("email").asText());
    newCred.setPassword(this.passwordEncoder.encode(node.get("password").asText()));
    newCred.setRole(node.get("role").asText());
    String randomCode = RandomString.make(64);
    newCred.setEnabled(false);
    newCred.setVerificationCode(randomCode);
    
    
    nuovo.setFirst_name(node.get("first_name").asText());
    nuovo.setLast_name(node.get("last_name").asText());

    nuovo.setProfile(node.get("profile").asText());
    nuovo.setSrc(node.get("src").asText());

    nuovo.setQuestionario(node.get("one").asText());
    newCred.setUser(nuovo);
    credentialservice.save(newCred);
    newUsers.add(newCred.getUser().getId());
    String url =  request.getRequestURL().toString().replace(request.getServletPath(), "") + "/verify?code=" + randomCode  ;
    this.credentialservice.sendVerificationEmail(newCred, url);
    return response;

  }
  
 
  @MessageMapping("/register")
  public void prova(@Payload Session session, SimpMessageHeaderAccessor headerAccessor) throws JsonProcessingException {

    UserController.ONLINE.put(session.getUser_id(), session.getSession());

    List < LessonEntity > lesson = this.lessonservice.getlesson(this.userservice.getUserId(session.getUser_id()));

    if (session.getLesson_id() != null) {
      System.out.println(this.credentialservice.getCredentialsUser(session.getUser_id()).getRole());
      if (this.credentialservice.getCredentialsUser(session.getUser_id()).getRole().equals("TEACHER")) {
        for (UserEntity u: this.lessonservice.lezionePerId(session.getLesson_id()).getFollowed_by()) {
          String n = String.valueOf(session.getLesson_id()) + String.valueOf(u.getId());
          LessonManager manager = MainController.LESSONS.get(n);
          send.notify(Starter.mapper.writeValueAsString(new Message.User(u.getId())), session.getSession());
          send.notify(Starter.mapper.writeValueAsString(new Message.Timeline(manager.geTimeline())), session.getSession());
        }
      } else {

        String n = String.valueOf(session.getLesson_id()) + String.valueOf(session.getUser_id());
        LessonManager manager = MainController.LESSONS.get(n);

        send.notify(Starter.mapper.writeValueAsString(new Message.Timeline(manager.geTimeline())), session.getSession());

        if (manager.st != null) {
          send.notify(Starter.mapper.writeValueAsString(MainController.LESSONS.get(n).st), UserController.ONLINE.get(session.getUser_id()));
        }
      }
    }

  }

  @PostMapping("/getstimulus")
  public String stimulus(@RequestBody ObjectNode node) {

    return "Done";
  }

  @PostMapping("/getprofile")
  public Response getProfile(@RequestBody ObjectNode node) throws JsonMappingException, JsonProcessingException {
    UserEntity nuovo = userservice.getUserId(node.get("id").asLong());
    Response response = new Response(nuovo.getProfile());
    System.out.println();
    return response;
  }

  @PostMapping("/findsuggestion")
  public List < RuleEntity > GetSuggestion(@RequestBody ObjectNode node) {
    System.out.println(node.get("ids").asLong());
    ModelEntity model = this.modelservice.getModel(node.get("ids").asLong());

    List < RuleEntity > re = new ArrayList < > ();
    for (RuleEntity r: model.getRules()) {
      List < SuggestionMongo > sug = new ArrayList < > ();
      RuleEntity rule = new RuleEntity();
      rule.setLength(r.getLength());
      rule.setId(r.getId());
      rule.setName(r.getName());
      if (r.getSuggestions() != null)
        sug = this.modelservice.getSuggestion(r.getSuggestions()).getSuggestion();
      Collections.sort(sug);
      rule.getSuggestionm().addAll(sug);
      re.add(rule);

    }
    return re;
  }

  @PostMapping("/findsuggestiontot")
  public List < Set < ObjectNode >> GetSuggestiontot(@RequestBody ObjectNode node) {
    System.out.println(node.get("ids").asLong());
    ModelEntity model = this.modelservice.getModel(node.get("ids").asLong());

    Set < RuleEntity > re = new HashSet < > ();
    Set < ObjectNode > nodes = new HashSet < > ();
    Set < ObjectNode > edges = new HashSet < > ();
    Set < ObjectNode > nodes2 = new HashSet < > ();
    Set < ObjectNode > nodes50 = new HashSet < > ();
    Set < ObjectNode > rules = new HashSet < > ();

    long i = 0;
    for (RuleEntity rule: model.getRules()) {
      ObjectNode robgr = objectMapper.createObjectNode();
      robgr.put("id", rule.getName());
      robgr.put("label", rule.getName());
      robgr.put("group", i);
      robgr.put("type", "rule");
      ObjectNode icon = objectMapper.createObjectNode();
      icon.put("face", "'FontAwesome'");
      icon.put("size", "50");
      if (rule instanceof WikiRuleEntity) {
        robgr.put("rule_type", "wiki");
        robgr.put("shape", "triangle");

      } else if (rule instanceof TextRuleEntity) {
        robgr.put("rule_type", "text");
        robgr.put("rule_text", this.ruleservice.getText(rule.getId()));
        robgr.put("image","..\\images\\LogoTesto.png");
        robgr.put("size", "30");
        robgr.put("shape", "image");
      } else if (rule instanceof WebRuleEntity) {
        robgr.put("rule_type", "web");
        robgr.put("rule_web", ((WebRuleEntity) rule).getUrl());
        robgr.put("image","..\\images\\www_icon.svg");
        robgr.put("size", "30");
        robgr.put("shape", "image");
        
      } else if (rule instanceof FileRuleEntity) {
        robgr.put("rule_type", "file");
        icon.put("code", "\uf15b");
        icon.put("color", "#000000");
        robgr.put("shape", "icon");
        robgr.put("icon", icon);

      }
      robgr.put("rule_id", rule.getId());
      nodes.add(robgr);
      nodes50.add(robgr);
      rules.add(robgr);
      i++;
    }
    i = 0;
    for (RuleEntity r: model.getRules()) {
      RuleEntity rule = new RuleEntity();
      rule.setLength(r.getLength());
      rule.setId(r.getId());
      rule.setName(r.getName());
      if (r.getSuggestions() != null)
        rule.getSuggestionm().addAll(this.modelservice.getSuggestion(r.getSuggestions()).getSuggestion());
      re.add(rule);
      List < Double > max = new ArrayList < > ();
      for (SuggestionMongo sm: rule.getSuggestionm()) {
        max.add(sm.getScore2());
      }
      if (r.getSuggestions() != null) {
        for (SuggestionMongo sm: rule.getSuggestionm()) {
          if (!containsName(nodes, sm.getPage())) {

            ObjectNode obj = objectMapper.createObjectNode();
            obj.put("id", sm.getPage());
            obj.put("label", sm.getPage());
            obj.put("group", i);
            obj.put("type", "sug");
            obj.put("score", sm.getScore2());
            obj.put("max", Collections.max(max));
            obj.put("rule_id", rule.getId());
            nodes.add(obj);
            ObjectNode obj2 = objectMapper.createObjectNode();
            obj2.put("id", sm.getPage());
            obj2.put("parent", rule.getName());
            obj2.put("rule_id", rule.getId());
            obj2.put("score1", sm.getScore());
            obj2.put("score2", sm.getScore2());
            obj2.put("type", "Suggestion");
            obj2.put("label", sm.getPage());
            obj2.put("group", i);
            nodes2.add(obj2);
            ObjectNode obj3 = objectMapper.createObjectNode();
            if (70 * Collections.max(max) / 100 <= sm.getScore2()) {
              obj3.put("id", sm.getPage());
              obj3.put("label", sm.getPage());
              obj3.put("group", i);
              obj3.put("type", "sug");
              obj3.put("score", sm.getScore2());
              nodes50.add(obj2);
            }
          }
          ObjectNode edge = objectMapper.createObjectNode();
          edge.put("from", rule.getName());
          edge.put("to", sm.getPage());
          edges.add(edge);
        }
      }
      for (RuleEntity effect: r.getPreconditions()) {
        ObjectNode edge = objectMapper.createObjectNode();
        edge.put("from", r.getName());
        edge.put("to", effect.getName());
        edge.put("arrows", "to");
        edge.put("width", "2");
        edges.add(edge);
      }
      i++;

    }

    List < Set < ObjectNode >> tot = new ArrayList < > ();
    tot.add(0, edges);
    tot.add(1, nodes);
    tot.add(2, nodes2);
    tot.add(3, rules);
    tot.add(4, nodes50);
    return tot;
  }

  public boolean containsName(final Set < ObjectNode > list, final String name) {
    return list.stream().filter(o -> o.get("id").asText().equalsIgnoreCase(name)).findFirst().isPresent();
  }

  @PostMapping("/edit")
  public Response edit(@RequestBody User user) {
    Response response = new Response("Done", user);
    UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Credentials credentials = credentialservice.getCredentials(userDetails.getUsername());
    UserEntity nuovo = credentials.getUser();
    credentials.setEmail(user.getEmail());
    nuovo.setFirst_name(user.getFirst_name());
    nuovo.setLast_name(user.getLast_name());
    credentialservice.save(credentials);

    return response;

  }

  @PostMapping("/changePassword")
  public String edit(@RequestBody ObjectNode node) {
    UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Credentials credentials = credentialservice.getCredentials(userDetails.getUsername());
    credentials.setPassword(this.passwordEncoder.encode(node.get("password").asText()));
    this.credentialservice.save(credentials);
    return "OK";

  }

  @PostMapping("/ciao")
  public String edit4() {
    System.out.println("mongo funziona??");
    return "prova";

  }

  @PostMapping("/edit_interests")
  public Response edit_interests(@RequestBody User user) throws JsonGenerationException, JsonMappingException, IOException {
    UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Credentials credentials = credentialservice.getCredentials(userDetails.getUsername());
    UserEntity nuovo = credentials.getUser();
    nuovo.setProfile(user.getProfile());
    userservice.saveUser(nuovo);
    Response response = new Response("Done", nuovo);
    return response;

  }

  @PostMapping("/uploadFileString")
  public Response uploadFileString(@RequestBody ObjectNode node) throws IllegalStateException, IOException {
    UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Credentials credentials = credentialservice.getCredentials(userDetails.getUsername());
    UserEntity nuovo = credentials.getUser();
    System.out.println(node.get("src").asText());
    nuovo.setSrc(node.get("src").asText());
    this.userservice.saveUser(nuovo);
    Response response = new Response("Done");
    return response;

  }

  @PostMapping("/uploadFileRule/{time}")
  public Long uploadfilerule(@RequestBody MultipartFile uploadfile,@PathVariable("time") Long time ) throws IllegalStateException, IOException {

    String file1 = uploadfile.getOriginalFilename();
    System.out.println(file1);
    String baseDir = System.getProperty("user.dir") + "//FileRule//";
    System.out.println(baseDir);
    uploadfile.transferTo(new File(baseDir + file1));
    RuleEntity rule = new FileRuleEntity();
    ((FileRuleEntity) rule).setSrc(baseDir + file1);

    rule.setName(m.getName());
    rule.setLength((long) time);
    this.ruleservice.saverule(rule);
    this.m.addRule(rule);
    this.modelservice.save(m);

    return m.getId();

  }

  @PostMapping("/uploadFileRulePre")
  public Long uploadfilerulepre(@RequestBody MultipartFile uploadfile) throws IllegalStateException, IOException {

    String file1 = uploadfile.getOriginalFilename();
    System.out.println(file1);
    String baseDir = System.getProperty("user.dir") + "//FileRule//";
    System.out.println(baseDir);
    uploadfile.transferTo(new File(baseDir + file1));
    RuleEntity rule = new FileRuleEntity();
    ((FileRuleEntity) rule).setSrc(baseDir + file1);
    this.ruleservice.saverule(rule);
    Fileid = rule.getId();
    return rule.getId();

  }

  @PostMapping("/uploadFileLesson/{id}")
  public Files uploadfilelesson(@RequestBody MultipartFile uploadfile, @PathVariable("id") Long id) throws IllegalStateException, IOException {
    UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Credentials credentials = credentialservice.getCredentials(userDetails.getUsername());
    UserEntity nuovo = credentials.getUser();
    String file1 = uploadfile.getOriginalFilename();
    String baseDir = System.getProperty("user.dir") + "//MaterialeDidattico//";
    Files f = new Files(file1);
    this.fileservice.save(f);
    uploadfile.transferTo(new File(baseDir + file1));
    f.setSrc(baseDir + file1);
    this.fileservice.save(f);
    System.out.println("LESSOn ID: " + id);
    LessonEntity lession = this.lessonservice.lezionePerId(id);
    lession.getFiles().add(f);
    this.lessonservice.save(lession);

    return f;

  }

  @RequestMapping("/file/{id}")
  public ResponseEntity < InputStreamResource > downloadFile1(
    @PathVariable("id") Long id) throws IOException {

    File file = new File(this.fileservice.filePerId(id).getSrc());
    InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

    return ResponseEntity.ok()
      // Content-Disposition
      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName())

      // Contet-Length
      .contentLength(file.length()) //
      .body(resource);
  }

  @RequestMapping("/fileRule/{id}")
  public ResponseEntity < InputStreamResource > downloadFile1Rule(
    @PathVariable("id") Long id) throws IOException {

    File file = new File(this.ruleservice.getFile(id));
    InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

    return ResponseEntity.ok()
      // Content-Disposition
      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName())

      // Contet-Length
      .contentLength(file.length()) //
      .body(resource);
  }

  @RequestMapping("/riddle/{id}")
  public ResponseEntity < InputStreamResource > downloadFileRiddle(
    @PathVariable("id") Long id) throws IOException {
    UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Credentials credentials = credentialservice.getCredentials(userDetails.getUsername());
    UserEntity nuovo = credentials.getUser();

    File file = new File(System.getProperty("user.dir") + "//riddle//" + id + nuovo.getId() + ".rddl");
    InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

    return ResponseEntity.ok()
      // Content-Disposition
      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName())

      // Contet-Length
      .contentLength(file.length()) //
      .body(resource);
  }

  @PostMapping("/uploadFile")
  public Response uploadfile(@RequestBody MultipartFile uploadfile) throws IllegalStateException, IOException {
    UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Credentials credentials = credentialservice.getCredentials(userDetails.getUsername());
    UserEntity nuovo = credentials.getUser();
    String file1 = uploadfile.getOriginalFilename();
    System.out.println(file1);
    String baseDir = System.getProperty("user.dir") + "//src//main//resources//static//images//";
    uploadfile.transferTo(new File(baseDir + nuovo.getId() + ".jpg"));
    nuovo.setSrc("\\images\\" + nuovo.getId() + ".jpg");
    this.userservice.saveUser(nuovo);
    Response response = new Response("Done");
    return response;

  }

  @PostMapping("/play")
  public Response PlayLesson(@RequestBody ObjectNode node) throws IllegalStateException, IOException {
    LessonEntity lesson = this.lessonservice.lezionePerId(node.get("id").asLong());
    if( lesson.getAsync()){
      UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      Credentials credentials = credentialservice.getCredentials(userDetails.getUsername());
      UserEntity user = credentials.getUser();
      String n = String.valueOf(node.get("id").asLong()) + String.valueOf(user.getId());
      System.out.println(n);
      System.out.println(LESSONS);
      if (LESSONS.get(n).getState() != LessonState.Running) {
        LESSONS.get(n).play();
      }
    }
    else {
      for (UserEntity u : lesson.getFollowed_by()) {
        String n = String.valueOf(node.get("id").asLong()) + String.valueOf(u.getId());
        System.out.println(n);
        System.out.println(LESSONS);
        if (LESSONS.get(n).getState() != LessonState.Running) {
          LESSONS.get(n).play();
        }
      }
    }
    Response response = new Response("Done");
    return response;

  }
  @PostMapping("/pause")
  public Response pauseLesson(@RequestBody ObjectNode node) throws IllegalStateException, IOException {
    LessonEntity lesson = this.lessonservice.lezionePerId(node.get("id").asLong());
    if( lesson.getAsync()){
      UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      Credentials credentials = credentialservice.getCredentials(userDetails.getUsername());
      UserEntity user = credentials.getUser();
      String n = String.valueOf(node.get("id").asLong()) + String.valueOf(user.getId());
      System.out.println(n);
      System.out.println(LESSONS);
      if (LESSONS.get(n).getState() != LessonState.Paused) {
        //LESSONS.get(n).pause();
      }
    }
    else {
      for (UserEntity u : lesson.getFollowed_by()) {
        String n = String.valueOf(node.get("id").asLong()) + String.valueOf(u.getId());
        System.out.println(n);
        System.out.println(LESSONS);
        if (LESSONS.get(n).getState() != LessonState.Paused) {
          //LESSONS.get(n).pause();
        }
      }
    }
    Response response = new Response("Done");
    return response;

  }

  @PostMapping("/stop")
  public Response stopLesson(@RequestBody ObjectNode node) throws IllegalStateException, IOException {
    LessonEntity lesson = this.lessonservice.lezionePerId(node.get("id").asLong());
    if( lesson.getAsync()){
      UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      Credentials credentials = credentialservice.getCredentials(userDetails.getUsername());
      UserEntity user = credentials.getUser();
      String n = String.valueOf(node.get("id").asLong()) + String.valueOf(user.getId());
      System.out.println(n);
      System.out.println(LESSONS);
      if (LESSONS.get(n).getState() != LessonState.Stopped) {
        //LESSONS.get(n).stop();
      }
    }
    else {
      for (UserEntity u : lesson.getFollowed_by()) {
        String n = String.valueOf(node.get("id").asLong()) + String.valueOf(u.getId());
        System.out.println(n);
        System.out.println(LESSONS);
        if (LESSONS.get(n).getState() != LessonState.Stopped) {
          //LESSONS.get(n).stop();
        }
      }
    }

    Response response = new Response("Done");
    return response;

  }

  @PostMapping("/Argomento/Getmodel")
  public Collection < RuleEntity > getmodel(@RequestBody ObjectNode node) {
    ModelEntity model = this.modelservice.getModel(node.get("ids").asLong());
    return model.getRules();

  }

  @PostMapping("/NewModel")
  public Response NewModel(@RequestBody String Model) {
    UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Credentials credentials = credentialservice.getCredentials(userDetails.getUsername());
    UserEntity nuovo = credentials.getUser();
    ModelEntity model = new ModelEntity();
    model.setName(Model);
    model.addTeacher(nuovo);
    
    modelservice.save(model);
    nuovo.getModels().add(model);
    ricerca.put(model.getId(), new ArrayList<>());
    this.m = model;
    Response response = new Response("Done", model);
    return response;

  }
  
  public Boolean checkName(ModelEntity m, String name) {
	for(RuleEntity r : m.getRules()) {
		if(r instanceof WikiRuleEntity ) {
		List<SuggestionMongo> s = this.modelservice.getSuggestion(r.getSuggestions()).getSuggestion();
		 for(SuggestionMongo sm : s) {
			 if(sm.getPage().equals(name) || r.getName().equals(name))
				 return false;
		 }
		}else {
			if(r.getName().equals(name))
				return false;
		}
		 
	}
	  
	  return true;  
  }

  @RequestMapping(value = "/Newrule", method = RequestMethod.POST)
  public Response NewModel(@RequestBody ObjectNode node, @RequestBody MultipartFile uploadfile) throws Exception {
	 
    RestTemplate restTemplate = new RestTemplate();
    boolean bool = true;
    UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Credentials credentials = credentialservice.getCredentials(userDetails.getUsername());
    UserEntity nuovo = credentials.getUser();

    ModelEntity model = this.m;
    final String name = node.get("rule_name").asText();
    JsonNode effect = node.get("rule_id");

    if (effect != null)
      model = this.modelservice.getModel(node.get("model_id").asLong());
    RuleMongo rulemongo = new RuleMongo();
    RuleEntity rule = null;
    
    if(ricerca.get(model.getId())==null) {
    	ricerca.put(model.getId(), new ArrayList<>());
    }
    System.out.println(ricerca);
    if(!ricerca.get(model.getId()).contains(name)) { 
    	
    switch (node.get("rule_type").asText()) {
    case "Testo":
      rule = new TextRuleEntity();
      if(checkName(model, name)) {
      ((TextRuleEntity) rule).setText(node.get("rule_text").asText());
      rule.setLength(node.get("rule_length").asLong());
      this.ruleservice.saverule(rule);
      }else {
    	  Response response = new Response("NO");
    	  return response;
      }
      break;
    case "Pagina Web":
    	 if(checkName(model, name)) {
      rule = new WebRuleEntity();
      ((WebRuleEntity) rule).setUrl(node.get("rule_text").asText());
      rule.setLength(node.get("rule_length").asLong());
      this.ruleservice.saverule(rule);
    	 }else {
    		 Response response = new Response("NO");
       	  return response;
    	 }
      break;
    case "Pagina Wikipedia":
      rule = new WikiRuleEntity();
      SuggestionM sm = new SuggestionM();
      if (this.modelservice.getRuleMongoByTitle(name)== null) {
    	  ricerca.get(model.getId()).add(name);
    	    send.notify(Starter.mapper.writeValueAsString(new Message.Searching(name, 0)), UserController.ONLINE.get(nuovo.getId()));
    	    WikiRestResponse wikiRestResponse;
    	    try {
         wikiRestResponse = restTemplate.getForObject("http://localhost:5015/wiki?page=" + name.replace(' ', '_'), WikiRestResponse.class);
    	    }
    	    catch(Exception e){
    	    	return new Response("Error","Python Server Error");
    	    }
    	    
        if (wikiRestResponse.getExists()) {
          ((WikiRuleEntity) rule).setUrl(wikiRestResponse.getUrl());
          List < RuleSuggestionRelationEntity > relations = new ArrayList < > ();

          int i = -1;
          for (String pre: wikiRestResponse.getPreconditions()) {
            i++;
            SuggestionMongo sugmongo = new SuggestionMongo();

            sugmongo.setPage(pre);

            sugmongo.setScore(Math.round((wikiRestResponse.getRank1().get(i).doubleValue()) * 100.0) / 100.0);
            sugmongo.setScore2(Math.round((wikiRestResponse.getRank2().get(i).doubleValue()) * 100.0) / 100.0);

            rulemongo.getSuggestions().add(sugmongo);
            sm.getSuggestion().add(sugmongo);

          }
          this.modelservice.saveSuggestionMongo(sm);
          rule.setSuggestion(sm.getId());
          rule.setLength(wikiRestResponse.getLength());
          rule.getTopics().addAll(wikiRestResponse.getCategories());
          rulemongo.setLength(wikiRestResponse.getLength());
          rulemongo.getTopics().addAll(wikiRestResponse.getCategories());
          rulemongo.setPlain_text(wikiRestResponse.getPlain_text());
        } else {
          System.out.println("ELEMENTO NON TROVATO PROVA CON " + wikiRestResponse.getSuggest() + " " + wikiRestResponse.getMaybe());
          Response response = new Response(wikiRestResponse.getExists(), wikiRestResponse.getSuggest(), wikiRestResponse.getMaybe(),model.getId());
          return response;
        }
      } else {
        bool = false;
        System.out.println("non sono entrato");
        List < RuleSuggestionRelationEntity > relations = new ArrayList < > ();
        RuleMongo m = this.modelservice.getRuleMongoByTitle(name);
        rule.setLength(m.getLength());
        rule.setName(name);
        for (SuggestionMongo s: m.getSuggestions()) {
          sm.getSuggestion().add(s);

        }

        this.modelservice.saveSuggestionMongo(sm);
        rule.setSuggestion(sm.getId());
        rule.getTopics().addAll(m.getTopics());
        ((WikiRuleEntity) rule).setUrl("https://it.wikipedia.org/wiki/" + name);

      }
    

      break;
    case "File":
    	 if(checkName(model, name)) {
      rule = this.ruleservice.getRule(Fileid);
      rule.setName(name);
      rule.setLength(node.get("rule_length").asLong());
      this.ruleservice.saverule(rule);
    	 }
    	 else {
    		 Response response = new Response("NO");
       	  return response;
    	 }
      break;
    }
    
    if (effect != null) {
      final RuleEntity effect_entity = this.ruleservice.getRule(effect.asLong());
      if (effect_entity == null)
        throw new Exception();
      rule.addEffect(effect_entity);
      effect_entity.addPrecondition(rule);

      if (effect_entity instanceof WikiRuleEntity) {
        System.out.println("sono qui!!");
        SuggestionM sm = this.modelservice.getSuggestion(effect_entity.getSuggestions());
        Optional < SuggestionMongo > relation = sm.getSuggestion().stream()
          .filter(s -> s.getPage().equalsIgnoreCase(name)).findAny();
        relation.ifPresent(rel -> {
          System.out.println(rel);
          sm.getSuggestion().remove(rel);
          this.modelservice.saveSuggestionMongo(sm);
        });
      }
      this.ruleservice.saverule(effect_entity);
    }
    this.ruleservice.saverule(rule);
      if (bool) {
        rule.setName(node.get("rule_name").asText());
        rulemongo.setTitle(name);;
        rulemongo.setId(rule.getId());
        rulemongorep.save(rulemongo);
      }
    System.out.println(model.getRules());
    model.getRules().add(rule);
    System.out.println("SONOQUIII");
    System.out.println(model.getRules());
    this.modelservice.save(model);
    send.notify(Starter.mapper.writeValueAsString(new Message.Searching(name, 1)), UserController.ONLINE.get(nuovo.getId()));
    ricerca.get(model.getId()).remove(name);
    Response response = new Response(true, model.getId(), rule.getId(), rule.getName());
    return response;
  }
    Response response = new Response("Exist");
	return response;


	 
  }
//  @RequestMapping(value = "/wikiQuestion/{id}", method = RequestMethod.GET)
//  public ArrayList<String> wikiQuestion(@PathVariable("id") Long id) {
//    RestTemplate restTemplate = new RestTemplate();
//    WikiQuestionResponse wikiQuestionResponse;
//    wikiQuestionResponse = restTemplate.getForObject("http://localhost:5015/wiki?page=" + name.replace(' ', '_'), WikiQuestionResponse.class);
//
//    return ricerca.get(id);
//  }
  @RequestMapping(value = "/ricerca/{id}", method = RequestMethod.POST)
  public ArrayList<String> ricerche(@PathVariable("id") Long id) {
	  if(!ricerca.containsKey(id)) {
		  return new ArrayList<>();
	  }
    return ricerca.get(id);
  }
  
  @RequestMapping(value = "/ricerchetot", method = RequestMethod.GET)
  public ObjectNode ricerchetot() {
	  ObjectMapper mapper = new ObjectMapper();
	  ObjectNode ricerche = objectMapper.createObjectNode();
	  ObjectNode array = objectMapper.createObjectNode();
	  for(Long l : ricerca.keySet()) {	
		  
		  ArrayNode arrayNode = mapper.createArrayNode();
		  for(String s : ricerca.get(l))
		  arrayNode.add(s);
		  
	 ricerche.put(l.toString(), arrayNode);
	  }
	  
    return ricerche;
  }
  
  @RequestMapping(value = "/deletemodel/{id}", method = RequestMethod.POST)
  public String deletemodel(@PathVariable("id") Long id) throws JsonProcessingException {
    System.out.println("OKK1");
    UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Credentials credentials = credentialservice.getCredentials(userDetails.getUsername());
    UserEntity userentity = credentials.getUser();
    System.out.println(userentity);
    for(RuleEntity r : this.modelservice.getModel(id).getRules())
        send.notify(Starter.mapper.writeValueAsString(new Message.Searching(r.getName(), -1)), UserController.ONLINE.get(userentity.getId()));
    this.modelservice.delete(id, userentity);
    ricerca.remove(id);
    System.out.println("OKK");
   
    return "OK";
  }

  @RequestMapping(value = "/deletelesson/{id}", method = RequestMethod.POST)
  public String deletelesson(@PathVariable("id") Long id) {
    UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Credentials credentials = credentialservice.getCredentials(userDetails.getUsername());
    UserEntity userentity = credentials.getUser();
    this.lessonservice.delete(this.lessonservice.lezionePerId(id));
    System.out.println("OKK");
    return "OK";
  }
  
  
  @RequestMapping(value = "/deleterule", method = RequestMethod.POST)
  public Response deleteRule(@RequestBody ObjectNode node) throws JsonMappingException, JsonProcessingException {
	  this.ruleservice.delete(node.get("model").asLong(), node.get("rule").asLong());
	  Response response = new Response("OK");
    return response;
  }
  

  @RequestMapping(value = "/getrulecat/{id}", method = RequestMethod.GET)
  public Collection < RuleEntity > getrulecat(@PathVariable("id") Long id) {

    return this.lessonservice.lezionePerId(id).getGoals();
  }

  @RequestMapping(value = "/NewLesson", method = RequestMethod.POST)
  public Response Newlesson(@RequestBody ObjectNode node) throws JsonMappingException, JsonProcessingException {
    UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Credentials credentials = credentialservice.getCredentials(userDetails.getUsername());
    UserEntity nuovo = credentials.getUser();
    LessonEntity lesson = new LessonEntity();

    lesson.setName(node.get("name").asText());
    lesson.setModel(modelservice.getModel(node.get("models").asLong()));

    ObjectMapper mapper = new ObjectMapper();

    lesson.setTeacher(nuovo);
    //List<Integer> goals = mapper.readValue(node.get("goals").asText(), List.class);
    List < Long > goals = mapper.readValue(node.get("goals").asText(), new TypeReference < List < Long >> () {});


    Boolean b = false;
    if(node.get("type").asInt() == 1)
      b=true;
    System.out.println("User subscribe :  " + b);
    lesson.setAsync(b);
    lesson = lessonservice.save(lesson);
    nuovo.getTeaching_lessons().add(lesson);
    userservice.saveUser(nuovo);
    for (Long g: goals) {
      System.out.println("ID " + Long.valueOf(g));
      RuleEntity rule = this.ruleservice.getRule(Long.valueOf(g));
      lesson.getGoals().add(rule);
      if(!this.quizQuestionService.existsQuizQuestionList(rule.getId())) {
        new Thread(new Runnable() {
          @Override
          public void run() {
            try {
              generateQuestion(rule.getName(), rule.getId());
            } catch (JsonProcessingException e) {
              throw new RuntimeException(e);
            }
          }
        }).start();
      }
    }
    Response response = new Response("Newlesson", lesson);

    long[] map = Starter.mapper.readValue(node.get("students").asText(), long[].class);
    List < UserEntity > u = new ArrayList < > ();
    for (long id: map) {
      UserEntity student = this.userservice.getUserId(Long.valueOf(id));
      u.add(student);
      lesson.addStudent(student);
      student.getFollowing_lessons().add(lesson);
      this.userrepository.save(student);
      System.out.println("User subscribe :  " + id);
      String n = String.valueOf(lesson.getId()) + String.valueOf(Long.valueOf(id));
      final LessonManager lesson_manager = new LessonManager(lesson, send, this.modelservice, this.userservice,this.ruleservice);
      System.out.println("NUMERO" + n);
      LESSONS.put(n, lesson_manager);
      lesson_manager.Solve();
      lesson.getFollowed_by().clear();
    }
    lesson.getFollowed_by().addAll(u);
    lessonservice.save(lesson);
    System.out.println(UserController.ONLINE);
    for (UserEntity us: lesson.getFollowed_by()) {
      System.out.println("1");
      if (UserController.ONLINE.get(us.getId()) != null) {
        System.out.println("2");
        send.notify(Starter.mapper.writeValueAsString(new Message.Lesson(lesson.getId(), lesson.getName(), lesson.getTeacher().getFirst_name() + " " + lesson.getTeacher().getLast_name(), lesson.getTeacher().getId())), UserController.ONLINE.get(us.getId()));
        send.notify(Starter.mapper.writeValueAsString(new Message.Subscribe(lesson.getId(), lesson.getName())), UserController.ONLINE.get(us.getId()));
        System.out.println("3");
      }
    }
    return response;

  }

  @PostMapping("/iscrizione")
  public Response iscrizione(@RequestBody Long id) {
    Response response = new Response("Done");
    UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Credentials credentials = credentialservice.getCredentials(userDetails.getUsername());
    UserEntity nuovo = credentials.getUser();
    LessonEntity l = lessonservice.lezionePerId(id);
    if (nuovo.getFollowing_lessons().contains(l)) {
      response.setStatus("exist");
    } else {
      l.addStudent(nuovo);
      nuovo.getFollowing_lessons().add(l);
      userservice.saveUser(nuovo);
      this.l = l;
    }

    return response;

  }

  @GetMapping("/Getlessons")
  public List < Model > getAllModel() {
    UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Credentials credentials = credentialservice.getCredentials(userDetails.getUsername());
    UserEntity nuovo = credentials.getUser();
    List<Model> m = this.modelrepository.findByTeachersonly(nuovo.getId());
    return m;

  }

  /*@PostMapping("/Argomento/Getlessonsreal")
	public List<LessonEntity> getAlllessons(@RequestBody ObjectNode node){
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    	UserEntity nuovo =  userservice.getUser(userDetails.getUsername());
    	List<ModelEntity> m = modelservice.getModelTeacher(nuovo);
    	System.out.println("User subscribe :  " + node.get("id").asLong());
		return this.modelservice.getModel(node.get("id").asLong()).;
		
	}*/

  @GetMapping("/Getlessons_student")
  public Response Getlessons_student() {
    UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Credentials credentials = credentialservice.getCredentials(userDetails.getUsername());
    UserEntity nuovo = credentials.getUser();
    List < LessonEntity > l = nuovo.getFollowing_lessons();
    Response response = new Response("Done");
    for (Long u: UserController.ONLINE.keySet()) {
      System.out.println("ID: " + u);
      send.notify("prova", UserController.ONLINE.get(u));
    }
    return response;

  }

  @GetMapping("/provamessaggio")
  public String Prova43() {
    for (Long u: UserController.ONLINE.keySet()) {
      System.out.println("ID: " + u);
      send.notify("prova", UserController.ONLINE.get(u));
    }
    return "funge";
  }
  public static MessageHeaders createHeaders(String sessionId) {
    SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
    headerAccessor.setSessionId(sessionId);
    headerAccessor.setLeaveMutable(true);
    return headerAccessor.getMessageHeaders();
  }

  @GetMapping("/getstudents")
  public List < UserEntity > Getstudents() {

    return userservice.getTeacher("STUDENT");

  }

  @GetMapping("/json")
  public RuleMongo Getstudentsds() {

    return this.modelservice.getRuleMongoByTitle("piramide");

  }

  @GetMapping("lezione/lessons")
  public Response getlessons2() {
    UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Credentials credentials = credentialservice.getCredentials(userDetails.getUsername());
    UserEntity nuovo = credentials.getUser();
    LessonEntity n = new LessonEntity();
    n.setName("pippo");
    List < LessonEntity > l = new ArrayList < LessonEntity > ();
    l.addAll(lessonservice.getlesson(nuovo));

    Response response = new Response("Done", l);
    return response;

  }

  @PostMapping("/getname")
  public Response getName(@RequestBody ObjectNode node) {

    Response response = new Response(this.ruleservice.getRuleName(node.get("id").asLong()));
    return response;

  }

  @GetMapping("/lesson")
  public Response getlesson() {
    Response response = new Response("Done", this.m);
    return response;

  }

  @PostMapping("/getEmail")
  public Response email(@RequestBody ObjectNode node) {
    Response response = new Response("Done");
    System.out.println("prova");
    if (credentialservice.getUser(node.get("email").asText()) == null) {
      response.setStatus("Done");
    } else {
      response.setStatus("No");
    }

    return response;

  }

  @GetMapping("/generate")
  public List<QuizQuestion>  generateQuestion(@RequestParam(value = "lesson", required = false) Long lessonId) throws JsonProcessingException {
    LessonEntity lesson = this.lessonservice.lezionePerId(lessonId);
    List<QuizQuestion> quizQuestionList = new ArrayList<>();
    for(RuleEntity  rule: lesson.getGoals()){
      quizQuestionList.addAll(getQuizQuestionList(this.modelservice.getRuleMongoById(rule.getId())));
      LOG.info("FINISH----------GENERATE QUIZ FOR RULE : " + rule.getId() + "------------------");
    }
    LOG.info("FINISH----------GENERATE ALL QUIZ------------------");
    if(quizQuestionList.size() < 20){
      return shuffleList(quizQuestionList, quizQuestionList.size());
    }
    return shuffleList(quizQuestionList, 20);
  }

  private List<QuizQuestion> getQuizQuestionList(RuleMongo ruleMongo) {
    List<QuizQuestion>  quizQuestionList = new ArrayList<>();
    QuizQuestionList quizQuestionListMongo = new QuizQuestionList();
    String basePrompt = "Create 10 high school - italian level quiz based on the provided text.\n"
            + "You must strictly add here to the following json format without any errors:\n"
            +"[\n"
            + "{\n"
            + "    \"id\": \"*[ Insert id the index of question]*\",\n"
            + "    \"question\": \"*[ Insert the question ]*\",\n"
            + "    \"options\": {\n"
            + "        \"a\": \"*[ Option A ]*\",\n"
            + "        \"b\": \"*[ Option B ]*\",\n"
            + "        \"c\": \"*[ Option C ]*\",\n"
            + "        \"d\": \"*[ Option D ]*\"\n"
            + "    },\n"
            +"     \"source\": \"*[Write the exact line or passage from the provided text \n" +
            "where the information for this question can be found.]*\",\n"
            + "    \"correct_answer\": \"*[Insert the letter corresponding to the correct answer, for example: 'c)']*\"\n"
            + "},\n"
            + "// ... more questions ...\n"
            + "]\n"
            + "Please note that you are allowed to modify only the parts within\n"
            + "brackets (*[...]*) in the format provided.\n"
            + "Ensure that all four options are distinct.\n"
            + "When mentioning a date, please make sure to specify the year.\n"
            + "The text is: \n";

    String paragraph = ruleMongo.getPlain_text();
    boolean validResult = true;
    int maxRetry = 0;
    quizQuestionList =this.quizQuestionService.getQuizQuestionsByRule(ruleMongo.getId());
    if(quizQuestionList == null) {
      while (validResult && maxRetry == 4) {
        LOG.info("START----------GENERATE QUIZ------------------");
        try {
          maxRetry++;
          LOG.info("START----------chatBotRest------------------");
          ObjectNode jsonObject = chatBotRest(basePrompt, paragraph);
          LOG.info("FINISH----------chatBotRest------------------");
          LOG.info("START----------Formatting question------------------");
          quizQuestionList = getQuestionAnswer(jsonObject.get("choices").get(0).toString());
          LOG.info("FINISH----------Formatting question------------------");
          validResult = false;
          quizQuestionListMongo.setRuleId(ruleMongo.getId());
          quizQuestionListMongo.setQuizQuestions(createQuizId(quizQuestionList, String.valueOf(ruleMongo.getId())));
          this.quizQuestionService.save(quizQuestionListMongo);
        } catch (Exception e) {
          LOG.error("------------ERROR RETRY JSON PARSING------------");
          LOG.error("ERRORE", e);

        }
      }
    }
    return quizQuestionList;
  }
  public static List<QuizQuestion> shuffleList(List<QuizQuestion> lista, int numeroElementi) {
    // Mescola la lista
    Collections.shuffle(lista);

    // Restituisce i primi numeroElementi elementi
    return lista.subList(0, numeroElementi);
  }

  public QuizQuestionList  generateQuestion(String ruleName, Long ruleId) throws JsonProcessingException {
    QuizQuestionList quizQuestionList = new QuizQuestionList();
    List<QuizQuestion> questionsanswer = new ArrayList<QuizQuestion>();
    RuleMongo ruleMongo = this.modelservice.getRuleMongoByTitle(ruleName);
    String basePrompt = "Create 10 high school - italian level quiz based on the provided text.\n"
            + "You must strictly add here to the following json format without any errors:\n"
            +"[\n"
            + "{\n"
            + "    \"id\": \"*[ Insert id the index of question]*\",\n"
            + "    \"question\": \"*[ Insert the question ]*\",\n"
            + "    \"options\": {\n"
            + "        \"a\": \"*[ Option A ]*\",\n"
            + "        \"b\": \"*[ Option B ]*\",\n"
            + "        \"c\": \"*[ Option C ]*\",\n"
            + "        \"d\": \"*[ Option D ]*\"\n"
            + "    },\n"
            +"     \"source\": \"*[Write the exact line or passage from the provided text \n" +
            "where the information for this question can be found.]*\",\n"
            + "    \"correct_answer\": \"*[Insert the letter corresponding to the correct answer, for example: 'c)']*\"\n"
            + "},\n"
            + "// ... more questions ...\n"
            + "]\n"
            + "Please note that you are allowed to modify only the parts within\n"
            + "brackets (*[...]*) in the format provided.\n"
            + "Ensure that all four options are distinct.\n"
            + "When mentioning a date, please make sure to specify the year.\n"
            + "The text is: \n";

    String paragraph = ruleMongo.getPlain_text();
    boolean validResult = true;
    while (validResult) {
      LOG.info("START----------GENERATE QUIZ------------------");
      try {
        LOG.info("START----------chatBotRest------------------");
        ObjectNode jsonObject = chatBotRest(basePrompt, paragraph);
        LOG.info("FINISH----------chatBotRest------------------");
        LOG.info("START----------Formatting question------------------");
        questionsanswer = getQuestionAnswer(jsonObject.get("choices").get(0).toString());
        LOG.info("FINISH----------Formatting question------------------");
        validResult = false;
      } catch (Exception e) {
        LOG.error("------------ERROR RETRY JSON PARSING------------");
        LOG.error("ERROR MESSAGE",e);
      }
    }
    LOG.info("FINISH----------GENERATE QUIZ------------------");
    quizQuestionList.setRuleId(ruleId);
    quizQuestionList.setQuizQuestions(createQuizId(questionsanswer, String.valueOf(ruleId)));
    this.quizQuestionService.save(quizQuestionList);
    LOG.info("---------------SAVED NEW QUIZZ FOR RULE " + ruleId +" ---------------");

    return quizQuestionList;
  }

  private ObjectNode chatBotRest(String basePrompt, String paragraph) {
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    QuestionGeneratorRequest questionGeneratorRequest = new QuestionGeneratorRequest();
    headers.set("Authorization", "Bearer " + chatBotKey);
    questionGeneratorRequest.setTemperature(0.5);
    questionGeneratorRequest.setModel("meta-llama/llama-3.1-8b-instruct");
    Messages messages = new Messages();
    messages.setRole("user");
    messages.setContent(basePrompt + paragraph);
    questionGeneratorRequest.getMessages().add(messages);
    HttpEntity<QuestionGeneratorRequest> request = new HttpEntity<>(questionGeneratorRequest, headers);
    ObjectNode jsonObject = restTemplate.postForObject(chatBotUrl, request, ObjectNode.class);
    return jsonObject;
  }

  public List<QuizQuestion> getQuestionAnswer(String text) throws JsonProcessingException {

    JSONObject jsonObject = new JSONObject(text);
    JSONObject message = jsonObject.getJSONObject("message");
    String  content = message.getString("content");
    String json = null;
    System.out.println(content);
        json = extractJSON(content);
    ObjectMapper mapper = new ObjectMapper();
    List<QuizQuestion> questions = mapper.readValue(json, new TypeReference<List<QuizQuestion>>(){});
    return questions;
  }

  public List<QuizQuestion> createQuizId(List<QuizQuestion> questions, String ruleId){
    List<QuizQuestion> newQuestions = new ArrayList<>();
    for(QuizQuestion question : questions){
      String id = question.getId();
      question.setId(ruleId + "|" +id);
      newQuestions.add(question);
    }
    return newQuestions;
  }

  public static String extractJSON(String text) {
    StringBuilder jsonBlocks = new StringBuilder();
    StringBuilder currentJsonBlock = new StringBuilder();
    Stack<Character> stack = new Stack<>();
    boolean insideJson = false;

    for (char c : text.toCharArray()) {
      if (c == '{' || c == '[') {
        stack.push(c);
        insideJson = true;
      }

      if (insideJson) {
        currentJsonBlock.append(c);
      }

      if (c == '}' || c == ']') {
        if (!stack.isEmpty() && ((c == '}' && stack.peek() == '{') || (c == ']' && stack.peek() == '['))) {
          stack.pop();
        }

        if (stack.isEmpty() && insideJson) {
          insideJson = false;
          jsonBlocks.append(currentJsonBlock.toString().trim()).append("\n");
          currentJsonBlock.setLength(0); // Reset the StringBuilder for the next JSON block
        }
      }
    }
    if (insideJson) {
      String completedJson = fixMissingBrackets(currentJsonBlock.toString().trim());
      jsonBlocks.append(completedJson);
    }

    return jsonBlocks.toString().trim();
  }

  private static String fixMissingBrackets(String jsonBlock) {
    int openBraces = 0, closeBraces = 0;
    int openBrackets = 0, closeBrackets = 0;

    // Conta parentesi aperte e chiuse
    for (char c : jsonBlock.toCharArray()) {
      if (c == '[') openBrackets++;
      if (c == ']') closeBrackets++;
    }

    // Aggiunge le parentesi mancanti
    StringBuilder correctedJson = new StringBuilder(jsonBlock);
    while (closeBrackets < openBrackets) {
      correctedJson.append(']');
      closeBrackets++;
    }

    return correctedJson.toString();
  }
  
//  @Autowired
//  private CredentialService credentialService;
//  
//  // Altri metodi del controller...
//  
//  @GetMapping("/subscribe/{userId}")
//  public String subscribeUser(@PathVariable Long userId, Model model) {
//      Credentials user = credentialService.subscribeUser(userId);
//      if (user != null) {
//          model.addAttribute("message", "Utente iscritto con successo");
//      } else {
//          model.addAttribute("message", "Impossibile iscrivere l'utente");
//      }
//      return "redirect:/userDetails/" + userId;
//  }
//
//  @GetMapping("/unsubscribe/{userId}")
//  public String unsubscribeUser(@PathVariable Long userId, Model model) {
//      Credentials user = credentialService.unsubscribeUser(userId);
//      if (user != null) {
//          model.addAttribute("message", "Utente disiscritto con successo");
//      } else {
//          model.addAttribute("message", "Impossibile disiscrivere l'utente");
//      }
//      return "redirect:/userDetails/" + userId;
//  }
  
  @PostMapping("/chekQuiz")
  public List<QuizQuestion> checkQuiz(@RequestBody ArrayNode nodes){
      List<QuizQuestion> resultQuizs = new ArrayList<>();
    Map<String, List<String>> groupedById = new HashMap<>();
    Map<String, String> givenAnswers = new HashMap<>();
    for (JsonNode node : nodes) {
      String question = node.get("question").asText(); // Ottieni la domanda
      String value = node.get("value").isNull() ? null : node.get("value").asText(); // Ottieni la risposta

      // Divide la chiave in ID e numero della domanda
      String[] keyParts = question.split("\\|");
      String id = keyParts[0]; // Ottieni l'ID

      // Aggiungi la chiave alla lista dell'ID corrispondente
      groupedById.computeIfAbsent(id, k -> new ArrayList<>()).add(question);

      // Memorizza la risposta fornita
      givenAnswers.put(question, value);
    }
    for (Map.Entry<String, List<String>> entry : groupedById.entrySet()) {
      String id = entry.getKey();
      List<String> questionsInMap = entry.getValue();
      List<QuizQuestion> correctAnswers = this.quizQuestionService.getQuizQuestionsByRule(Long.valueOf(id));
      System.out.println("Verifica per ID: " + id);

      // Controlla ogni domanda per questo ID
      for (String questionKey : questionsInMap) {
        QuizQuestion question = correctAnswers.stream().filter(ans -> ans.getId().equals(questionKey)).findAny().get();
        String correctAnswer = question.getCorrectAnswer();
        String givenAnswer = givenAnswers.get(questionKey);

        QuizQuestion resultQuiz = new QuizQuestion();
        resultQuiz.setId(questionKey);
        resultQuiz.setCorrectAnswer(correctAnswer);
        resultQuiz.setSource(givenAnswer);
        resultQuiz.setQuestion(question.getQuestion());
        resultQuiz.setOptions(question.getOptions());
        resultQuizs.add(resultQuiz);
        // Stampa i risultati
        //System.out.println("Numero della domanda: " + questionNumber + " - Risposta corretta: " + isCorrect);
      }
    }


    return resultQuizs;
  }
  
  
}