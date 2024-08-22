package it.cnr.istc.psts.wikitel.Repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import it.cnr.istc.psts.wikitel.db.Files;


public interface FilesRepository extends CrudRepository<Files, Long> {
	
	public List<Files> findAll();

}
