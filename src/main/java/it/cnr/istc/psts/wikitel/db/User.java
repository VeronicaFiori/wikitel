package it.cnr.istc.psts.wikitel.db;

import java.util.List;

import javax.persistence.OneToMany;

import lombok.Data;

@Data
public class User  {

	private String email;
	private String password;
    private String first_name;
    private String last_name;
    private String profile;
	
    
    @OneToMany
    private List<StudentLesson> studenti;
}
