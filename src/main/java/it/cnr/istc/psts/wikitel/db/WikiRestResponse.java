package it.cnr.istc.psts.wikitel.db;

import lombok.Data;

import java.util.List;
import java.util.Set;
@Data
public class WikiRestResponse {

	private Set<String> categories;
	private long length;
	private List<String> preconditions;
	private List<Float> rank1;
	private List<Float> rank2;
	private String url;
	private Boolean exists;
	private String suggest;
	private List<String> maybe;
	private String plain_text;
}
