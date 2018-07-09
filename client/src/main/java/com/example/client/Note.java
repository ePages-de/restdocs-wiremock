package com.example.client;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Note {

	final UUID id;
	final String title;

	@JsonCreator
	public Note(@JsonProperty("id") UUID id, @JsonProperty("title") String title) {
		this.id = id;
		this.title = title;
	}

	public UUID getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}
}
