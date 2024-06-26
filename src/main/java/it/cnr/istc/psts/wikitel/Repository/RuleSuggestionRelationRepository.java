package it.cnr.istc.psts.wikitel.Repository;

import org.springframework.data.repository.CrudRepository;

import it.cnr.istc.psts.wikitel.db.RuleSuggestionRelationEntity;
import it.cnr.istc.psts.wikitel.db.RuleSuggestionRelationId;

public interface RuleSuggestionRelationRepository extends CrudRepository<RuleSuggestionRelationEntity, RuleSuggestionRelationId> {

}
