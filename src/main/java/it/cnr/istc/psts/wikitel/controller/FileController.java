package it.cnr.istc.psts.wikitel.controller;

import java.io.File;
import java.io.IOException;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FileController {

	@GetMapping("/RuleEntity/{fileName}")
	public ResponseEntity<Resource> getFile(@PathVariable String fileName) {
	    String baseDir = System.getProperty("user.dir") + "//FileRule//";
	    String filePath = baseDir + fileName;

	    File file = new File(filePath);

	    if (!file.exists()) {
	        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	    }

	    Resource resource = new FileSystemResource(file);
	    HttpHeaders headers = new HttpHeaders();
	  
	    headers.setContentType(MediaType.APPLICATION_PDF);

	    // Non impostare esplicitamente Content-Disposition, 
	    // lascia che il browser gestisca come visualizzare il contenuto
	    return new ResponseEntity<>(resource, headers, HttpStatus.OK);
	   
	}

}

