package it.cnr.istc.psts.wikitel.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import it.cnr.istc.psts.wikitel.db.Progress;

public interface ProgressRepository extends JpaRepository<Progress, Long> {
    Progress findByUserIdAndLezioneId(String string, String string2);
    
    Progress findByUserIdAndLezioneIdAndCurrentIndexAndCurrentLength(String string, String string2, int currentIndex, int currentLength);

}

