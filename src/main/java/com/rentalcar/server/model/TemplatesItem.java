package com.rentalcar.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TemplatesItem{

	@JsonProperty("createdAt")
	private String createdAt;

	@JsonProperty("sender")
	private Sender sender;

	@JsonProperty("subject")
	private String subject;

	@JsonProperty("modifiedAt")
	private String modifiedAt;

	@JsonProperty("toField")
	private String toField;

	@JsonProperty("name")
	private String name;

	@JsonProperty("replyTo")
	private String replyTo;

	@JsonProperty("id")
	private int id;

	@JsonProperty("tag")
	private String tag;

	@JsonProperty("isActive")
	private boolean isActive;

	@JsonProperty("testSent")
	private boolean testSent;

	@JsonProperty("htmlContent")
	private String htmlContent;

	public String getCreatedAt(){
		return createdAt;
	}

	public Sender getSender(){
		return sender;
	}

	public String getSubject(){
		return subject;
	}

	public String getModifiedAt(){
		return modifiedAt;
	}

	public String getToField(){
		return toField;
	}

	public String getName(){
		return name;
	}

	public String getReplyTo(){
		return replyTo;
	}

	public int getId(){
		return id;
	}

	public String getTag(){
		return tag;
	}

	public boolean isIsActive(){
		return isActive;
	}

	public boolean isTestSent(){
		return testSent;
	}

	public String getHtmlContent(){
		return htmlContent;
	}

	@Override
 	public String toString(){
		return 
			"TemplatesItem{" + 
			"createdAt = '" + createdAt + '\'' + 
			",sender = '" + sender + '\'' + 
			",subject = '" + subject + '\'' + 
			",modifiedAt = '" + modifiedAt + '\'' + 
			",toField = '" + toField + '\'' + 
			",name = '" + name + '\'' + 
			",replyTo = '" + replyTo + '\'' + 
			",id = '" + id + '\'' + 
			",tag = '" + tag + '\'' + 
			",isActive = '" + isActive + '\'' + 
			",testSent = '" + testSent + '\'' + 
			",htmlContent = '" + htmlContent + '\'' + 
			"}";
		}
}