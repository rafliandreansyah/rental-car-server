package com.rentalcar.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Sender{

	@JsonProperty("name")
	private String name;

	@JsonProperty("id")
	private int id;

	@JsonProperty("email")
	private String email;

	public String getName(){
		return name;
	}

	public int getId(){
		return id;
	}

	public String getEmail(){
		return email;
	}

	@Override
 	public String toString(){
		return 
			"Sender{" + 
			"name = '" + name + '\'' + 
			",id = '" + id + '\'' + 
			",email = '" + email + '\'' + 
			"}";
		}
}