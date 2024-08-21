package it.cnr.istc.psts.wikitel.Mongodb;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;
@Data
@Document("Rule")
public class RuleMongo {
	
	
	@Id
	private String id;
	
	private String title;
	
	private Long length;
	
	private  Set<SuggestionMongo> suggestions = new HashSet<>();
	 
	private Set<String> topics = new HashSet<>();
	
	private String plain_text;
	
	private boolean top_down = true;


}
