package com.rentalcar.server.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TemplateResponse {

	@JsonProperty("templates")
	private List<TemplatesItem> templates;

	@JsonProperty("count")
	private int count;

	public List<TemplatesItem> getTemplates(){
		return templates;
	}

	public int getCount(){
		return count;
	}

	@Override
 	public String toString(){
		return 
			"ResponseListTemplate{" + 
			"templates = '" + templates + '\'' + 
			",count = '" + count + '\'' + 
			"}";
		}
}