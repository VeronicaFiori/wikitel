package it.cnr.istc.psts.wikitel.db;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Embeddable;

import lombok.Data;

@Embeddable

@Data
public class StudentLessonId implements Serializable {

    private Long idLezione;
    private Long idStudente;

    public StudentLessonId() {}

    public StudentLessonId(Long idLezione, Long idStudente) {
        this.idLezione = idLezione;
        this.idStudente = idStudente;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentLessonId that = (StudentLessonId) o;
        return Objects.equals(idLezione, that.idLezione) && Objects.equals(idStudente, that.idStudente);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idLezione, idStudente);
    }
}
