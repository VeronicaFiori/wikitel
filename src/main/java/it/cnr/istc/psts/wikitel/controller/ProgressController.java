package it.cnr.istc.psts.wikitel.controller;


import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.cnr.istc.psts.wikitel.Service.ProgressService;
import it.cnr.istc.psts.wikitel.db.Progress;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    @Autowired
    private ProgressService progressService;

   

    
    
    @PostMapping("/save")
    public ResponseEntity<Map<String, String>> saveProgress(@RequestBody Progress progress) {
        try {
            // Salva il progresso
          //  progressService.save(progress);
        	progressService.saveOrUpdate(progress);
            // Restituisci una risposta JSON
            Map<String, String> response = new HashMap<>();
            response.put("message", "Progress saved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to save progress");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    
    @GetMapping("/get")
    public ResponseEntity<Progress> getProgress(@RequestParam String userId, @RequestParam String lezioneId) {
        try {
            Progress progress = progressService.getProgress(userId, lezioneId);
            if (progress != null) {
                return ResponseEntity.ok(progress);
            } else {
                return ResponseEntity.notFound().build();  // Se non trova il progress
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }




}

