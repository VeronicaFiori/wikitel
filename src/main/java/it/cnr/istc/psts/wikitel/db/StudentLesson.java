package it.cnr.istc.psts.wikitel.db;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

import it.cnr.istc.psts.wikitel.Service.Starter;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
public class StudentLesson {

    @EmbeddedId
    private StudentLessonId id;


    @ManyToMany
    private List<RuleEntity> argomenti= new ArrayList<>();

    
//    private boolean ycategory() {
//        for (RuleEntity arg : argomenti) {
//            for (final JsonNode interesse : Starter.USER_MODEL.get("interests")) {
//                if (interesse.asText().equals(arg.getName())) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
}

