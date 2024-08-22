package it.cnr.istc.psts.wikitel.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.cnr.istc.psts.wikitel.Repository.ProgressRepository;
import it.cnr.istc.psts.wikitel.db.Progress;

@Service
public class ProgressService {

    @Autowired
    private ProgressRepository progressRepository;

    public void saveOrUpdate(Progress progress) {
        // Cerca se esiste un record con la stessa combinazione di userId e lezioneId
        Progress existingProgress = progressRepository.findByUserIdAndLezioneId(
            progress.getUserId(),
            progress.getLezioneId()
        );

        if (existingProgress != null) {
            // Se esiste, aggiorna solo i campi necessari
            existingProgress.setCurrentIndex(progress.getCurrentIndex());
            existingProgress.setCurrentLength(progress.getCurrentLength());
            existingProgress.setPercentage(progress.getPercentage());
        } else {
            // Se non esiste, crea un nuovo record
            existingProgress = new Progress();
            existingProgress.setUserId(progress.getUserId());
            existingProgress.setLezioneId(progress.getLezioneId());
            existingProgress.setCurrentIndex(progress.getCurrentIndex());
            existingProgress.setCurrentLength(progress.getCurrentLength());
            existingProgress.setPercentage(progress.getPercentage());
        }

        progressRepository.save(existingProgress);
    }


    
    public Progress getProgress(String userId, String lezioneId) {
        Progress entity = progressRepository.findByUserIdAndLezioneId(userId, lezioneId);
//        if (entity != null) {
//            Progress dto = new Progress();
//            dto.setUserId(entity.getUserId());
//            dto.setLezioneId(entity.getLezioneId());
//            dto.setCurrentIndex(entity.getCurrentIndex());
//            dto.setCurrentLength(entity.getCurrentLength());
//            dto.setPercentage(entity.getPercentage());
//            return dto;
//        }
        return entity;
    }
    
    
}

