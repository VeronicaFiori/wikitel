package it.cnr.istc.psts.wikitel.controller;


import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.HashMap;
import java.util.Map;

@Data
public class UserController {
	 @Autowired
	 private SimpMessagingTemplate simpMessagingTemplate;

	public static final Map<Long,String> ONLINE = new HashMap<>();
	
	
	
	

}
