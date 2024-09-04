package it.cnr.istc.psts.wikitel.controller;


import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


import it.cnr.istc.psts.Websocket.Sending;
import it.cnr.istc.psts.wikitel.MongoRepository.RuleMongoRepository;
import it.cnr.istc.psts.wikitel.Repository.CredentialsRepository;
import it.cnr.istc.psts.wikitel.Repository.FilesRepository;
import it.cnr.istc.psts.wikitel.Repository.ModelRepository;
import it.cnr.istc.psts.wikitel.Repository.UserRepository;
import it.cnr.istc.psts.wikitel.Service.CredentialService;
import it.cnr.istc.psts.wikitel.Service.LessonService;
import it.cnr.istc.psts.wikitel.Service.ModelService;
import it.cnr.istc.psts.wikitel.Service.ProgressService;
import it.cnr.istc.psts.wikitel.Service.UserService;
import it.cnr.istc.psts.wikitel.db.Credentials;
import it.cnr.istc.psts.wikitel.db.FileRuleEntity;
import it.cnr.istc.psts.wikitel.db.Files;
import it.cnr.istc.psts.wikitel.db.LessonEntity;
import it.cnr.istc.psts.wikitel.db.ModelEntity;
import it.cnr.istc.psts.wikitel.db.Progress;
import it.cnr.istc.psts.wikitel.db.RuleEntity;
import it.cnr.istc.psts.wikitel.db.TextRuleEntity;
import it.cnr.istc.psts.wikitel.db.UserEntity;
import it.cnr.istc.psts.wikitel.db.WebRuleEntity;
import it.cnr.istc.psts.wikitel.db.WikiRuleEntity;





@Controller
public class pageController {
	

	
	private static final Object STUDENT_ROLE = null;

	@Autowired
	private UserService userservice;
	
	@Autowired
	private CredentialService credentialservice;
	
	
	@Autowired
	private RuleMongoRepository rulemongorep;
	
	@Autowired
	private LessonService lessonservice;
	
	@Autowired
	private ModelRepository modelrepository;
	
	@Autowired
	private Sending send;
	
	@Autowired
	private ModelService modelservice;

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private CredentialsRepository credentialsRepository;

	@Autowired
	private FilesRepository filesRepository;
	@Autowired
	private ProgressService progressService;
	 



	@RequestMapping(value = {"/","/index"}, method = RequestMethod.GET)
	public String index(Model model) throws NoSuchFieldException, IOException, InterruptedException {
		RestTemplate restTemplate = new RestTemplate();
		//Prova prova = restTemplate.getForObject("http://192.168.1.79:5015/wiki?page=Palombaro_lungo", Prova.class);
		//System.out.println(prova.getLength());
		Json_reader interests = json("/json/user_model.json",true);
		model.addAttribute("interests", interests.getInterests());
		System.out.println("ONLINE: "+UserController.ONLINE);
//		Process process = Runtime.getRuntime().exec("python3 -c 'import C:\\Users\\aliyo\\OneDrive\\Desktop\\python.py;python.prova() '");
//		process.waitFor();
//		int exitCode = process.exitValue();
//		System.out.println(exitCode);

			return "index";
	}
	
	 @GetMapping("/verify")
	  public String verify(@Param("code") String code,Model model) {
		 System.out.println(code);
		  if (this.credentialservice.verify(code)) {
			  model.addAttribute("complete", true);   
		    } else {
		    	model.addAttribute("complete", false);
		    }
		  return "registercode";
		  
	  }
	
	@RequestMapping(value = "/failure", method = RequestMethod.GET)
	public String failure(Model model) {
		//Json_reader interests = json("/json/user_model.json",true);
		System.out.println("CISONO");
		//model.addAttribute("interests", interests.getInterests());
		model.addAttribute("loginError", true);
			return "redirect:/index";
	}
	
	@RequestMapping(value = "/default", method = RequestMethod.GET)
    public String defaultAfterLogin(Model model) {
		Json_reader interests = json("/json/user_model.json",true);
		System.out.println("username1:PIPPO");
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Credentials credentials = credentialservice.getCredentials(userDetails.getUsername());
		UserEntity userentity = credentials.getUser();
    	model.addAttribute("model", this.modelrepository.findByTeachersonly(userentity.getId()));
    	model.addAttribute("interests", interests.getInterests());
    	model.addAttribute("user",userentity);
    	model.addAttribute("Teachers",userservice.getTeacher(userentity.TEACHER_ROLE));

    	if( MainController.newUsers.contains(userentity.getId()) ) {
    		model.addAttribute("first",true);
    	}
    	MainController.newUsers.remove(userentity.getId());
    	
    	if(credentials.isEnabled()) {
    	if (credentials.getRole().equals(UserEntity.STUDENT_ROLE)) {
    		HashMap<String,ArrayList<LessonEntity>> t = new HashMap<>();
    		for(LessonEntity l : userentity.getFollowing_lessons()) {
    			if(t.containsKey(l.getTeacher().getFirst_name() + " " + l.getTeacher().getLast_name() )) {
    				t.get(l.getTeacher().getFirst_name() + " " + l.getTeacher().getLast_name()).add(l);
    			}else {
    				t.put(l.getTeacher().getFirst_name() + " " + l.getTeacher().getLast_name(), new ArrayList<>());
    				t.get(l.getTeacher().getFirst_name() + " " + l.getTeacher().getLast_name()).add(l);
    			}
            	
            	

    		}
    		System.out.println(t);
    		model.addAttribute("lessons", t);
    		
    		/*per la progressbar**/
    		Map<String, Progress> progressMap = new HashMap<>();

    		for (String chiave : t.keySet()) {
    		    for (LessonEntity lezione : t.get(chiave)) {
    		        String lezioneIdStr = lezione.getId().toString();
    		        model.addAttribute("lezioneIdStr", lezioneIdStr);
    		        
    		        String userIdStr= userentity.getId().toString();
    		        model.addAttribute("userIdStr", userIdStr);
    		        Progress progress = this.progressService.getProgress(userIdStr, lezioneIdStr);
    		        progressMap.put(lezioneIdStr, progress);
    		    }
    		}

    		model.addAttribute("progressMap", progressMap);
    		
    		
    		
        	
            return "admin/hello";   //non deve essere admin 
        }
    	else  if (credentials.getRole().equals(credentials.ADMIN_ROLE)) {
    		return "admin/page.html";
    	}
    	
    	//se non lo e'
        return "teachers/index";
    	}
    	model.addAttribute("complete",false);
    	return "registercode";
    }
	
	@RequestMapping(value =  "/deletemodel/{id}" , method = RequestMethod.GET)
	 public String deletemodel(@PathVariable("id") Long id) {
		 UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			Credentials credentials = credentialservice.getCredentials(userDetails.getUsername());
			UserEntity userentity = credentials.getUser();
			this.modelservice.delete(id, userentity);
			System.out.println("OKK");
			return "redirect:/default";
	 }
	
	@RequestMapping(value = "/profile", method = RequestMethod.GET)
	public String profile(Model model) throws IOException {
		 ObjectMapper mapper = new ObjectMapper();
			UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	    	Credentials credentials = credentialservice.getCredentials(userDetails.getUsername());
			UserEntity userentity = credentials.getUser();
    	String name = userentity.getFirst_name();
    	System.out.println(name.substring(name.length() - 1));
    	List<LessonManager> m = new ArrayList<>();
    	Json_reader interests = json("/json/user_model.json",true);
    	model.addAttribute("gender",name.substring(name.length() - 1));
		model.addAttribute("all_interests",interests.getInterests());
    	model.addAttribute("user",userentity);
    	model.addAttribute("credentials",credentials);
    	for(LessonEntity l : userentity.getFollowing_lessons()) {
    		String n = String.valueOf(l.getId()) + String.valueOf(userentity.getId());
    		LessonManager manager = MainController.LESSONS.get(n);
    		m.add(manager);	
   	}
   
    	model.addAttribute("manager",m);
    	model.addAttribute("teacher",true);
			return "admin/profilo";
	}
	
	
	public static Json_reader json(String input,Boolean help) {
		Json_reader interests=new Json_reader();
		System.out.println("username: ");
		ObjectMapper mapper = new ObjectMapper();
		TypeReference<Json_reader> typeReference = new TypeReference<Json_reader>(){};
		InputStream inputStream = TypeReference.class.getResourceAsStream(input);
		System.out.println("username2: " + inputStream);
		
		try {
			 interests = mapper.readValue(inputStream,typeReference);
			 
			
		} catch (IOException e){
			System.out.println("Unable to save users: " + e.getMessage());
		}
		return interests;
		
	}

		
	@GetMapping(value = "/lezione/{id}")
	public String det_ordine(@PathVariable("id")Long id , Model model) throws JsonProcessingException {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    	Credentials credentials = credentialservice.getCredentials(userDetails.getUsername());
		LessonEntity lezione = lessonservice.lezionePerId(id);
		UserEntity userentity = credentials.getUser();
		System.out.println("USER: " + userentity.getId());
		System.out.println(credentials.getRole());
		System.out.println(lezione.getAsync());
		System.out.println(credentials.getRole().equals("STUDENT"));
    	model.addAttribute("user",userentity);
		if(lezione.getAsync() && credentials.getRole().equals("STUDENT")) {	
			model.addAttribute("role", "controller");
			System.out.println("USER:");
		}
		if (!lezione.getAsync() && credentials.getRole().equals("TEACHER")){
			model.addAttribute("role", "controller");
			System.out.println("Teacher:");
		}
		model.addAttribute("roles", credentials.getRole());
		model.addAttribute("files",lezione.getFiles());
		model.addAttribute("lezione",lezione);
		model.addAttribute("userId", userentity.getId());
		DateFormat df = new SimpleDateFormat("yy"); // Just the year, with 2 digits
		String formattedDate = df.format(Calendar.getInstance().getTime());
		DateFormat df2 = new SimpleDateFormat("yy"); // Just the year, with 2 digits
		System.out.println(formattedDate + "/" + (((Calendar.getInstance().get(Calendar.YEAR)+1))%100));
		model.addAttribute("anno",formattedDate + "/" + (((Calendar.getInstance().get(Calendar.YEAR)+1))%100));
		model.addAttribute("students",lezione.getFollowed_by());
		String n = String.valueOf(id) + String.valueOf(userentity.getId());
	
		/**/
		LessonManager lessonManager = new LessonManager(lezione, send, modelservice, userservice, null);
		List<RuleEntity> goals = lessonManager.getArgomentiPerStudenti();
		model.addAttribute("goalsl",goals);
		
		List<String> wiki = new ArrayList<>();
		List<String> web = new ArrayList<>();
		List<String> text = new ArrayList<>();
		List<String> file = new ArrayList<>();

		for(RuleEntity g : goals) {
		    if (g instanceof WebRuleEntity) {
		      //  web.add(((WebRuleEntity) g).getUrl());
		    	web.add(((WebRuleEntity) g).getName());
		        model.addAttribute("web",web);

		    } else if (g instanceof TextRuleEntity) {
		      //  text.add(((TextRuleEntity) g).getText());
		    	text.add(((TextRuleEntity) g).getName());
		        model.addAttribute("text",text);

		    } else if (g instanceof FileRuleEntity) {
		    //    file.add(((FileRuleEntity) g).getSrc());
		    	file.add(((FileRuleEntity) g).getName());
		        model.addAttribute("file",file);
		        
		    } else if (g instanceof WikiRuleEntity) {
		        wiki.add(g.getName());

		        model.addAttribute("wiki",wiki);
		    }

		}
		List<Files> filepdf= this.filesRepository.findAll();
        model.addAttribute("filepdf",filepdf);
        
		if(credentials.getRole().equals(STUDENT_ROLE)) { 
			Json_reader interests = json("/json/user_model.json",true);        
			System.out.println("PROVA : " + MainController.LESSONS);
		    model.addAttribute("messages",MainController.LESSONS.get(n).getStimuli(userentity.getId()));

		}
				
		
		Map<String, Progress> progressMap = new HashMap<>();

		String lezioneIdStr = lezione.getId().toString();
		model.addAttribute("lezioneIdStr", lezioneIdStr);

		for(UserEntity userId: lezione.getFollowed_by()) {
			String userIdStr = userId.getId().toString();
			model.addAttribute("userIdStr", userIdStr);
			Progress progress = this.progressService.getProgress(userIdStr, lezioneIdStr);
			progressMap.put(userIdStr, progress);


			model.addAttribute("progressMap", progressMap);
		}

		    
		return "teachers/lezione";
	}
	
	@GetMapping(value = "/Argomento/{id}")
	public String det_Arg(@PathVariable("id")Long id , Model model) {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    	Credentials credentials = credentialservice.getCredentials(userDetails.getUsername());
		UserEntity userentity = credentials.getUser();
		DateFormat df = new SimpleDateFormat("yy"); // Just the year, with 2 digits
		String formattedDate = df.format(Calendar.getInstance().getTime());
		DateFormat df2 = new SimpleDateFormat("yy"); // Just the year, with 2 digits
		System.out.println(formattedDate + "/" + (((Calendar.getInstance().get(Calendar.YEAR)+1))%100));
		ModelEntity m = modelservice.getModel(id);
		model.addAttribute("anno",formattedDate + "/" + (((Calendar.getInstance().get(Calendar.YEAR)+1))%100));
		model.addAttribute("students",credentialservice.getTeacher("STUDENT"));
		model.addAttribute("arg",m);
		model.addAttribute("name", m.getName());
		model.addAttribute("goal",m.getRules());
		model.addAttribute("lesson",this.lessonservice.getlessonbymodel(this.modelservice.getModel(id)));
		model.addAttribute("user",userentity);
		

		return "teachers/Argomento";
	}
	
	@GetMapping(value = "/profile/{id}")
	public String det_profilo(@PathVariable(required = false) Long id, Model model) {
		UserEntity user = userservice.getUserId(id); 
		Credentials c = credentialservice.getCredentialsUser(id);
    	model.addAttribute("user",user);
    	model.addAttribute("teacher",false);
    	model.addAttribute("credentials",c);
		return "admin/profilo";
	}
	
	/******/
	/*ADMIN */
	@GetMapping("/utenti")
	public String getCuochiLoggedIn(Model model) {	
		
		model.addAttribute("user", this.userRepository.findByRole(UserEntity.STUDENT_ROLE));
		return "/admin/utenti.html";
	}
	
	/*SOLO ADMIN PUO ELIMINARE GLI user*/
    @Transactional

	@GetMapping("/deleteUser/{id}")
	public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            // Elimina le credenziali associate
//        	lessonservice.deleteLessonByUser(id);
        	UserEntity userEntity = userservice.getUserId(id);
        	List<LessonEntity> lessons= lessonservice.getlesson(userEntity);
        	for(LessonEntity l: lessons) {
        		lessonservice.delete(l);
           	}
     
            credentialsRepository.deleteByUserId(id);
   
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			Credentials credentials = credentialservice.getCredentials(userDetails.getUsername());
            if (credentials.getRole().equals(credentials.ADMIN_ROLE)) {
            	return "redirect:/utenti";
            }
            	 
            // Elimina l'utente
        } catch (Exception e) {
            // Gestisci l'eccezione come necessario, ad esempio loggandola
            // Puoi rilanciare l'eccezione se necessario
            throw e;
        }
        

        return "redirect:/";
	}
	
}
