package com.ousmanetaram.springbatchintegration.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


public class RainItem implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private LocalDateTime eventDate;

	private String rainType;

	private String name;

	private Set<PinPoint> pinpoints = new HashSet<>();

	public RainItem() {
		super();
	}

	public LocalDateTime getEventDate() {
		return eventDate;
	}

	public void setEventDate(LocalDateTime eventDate) {
		this.eventDate = eventDate;
	}

	public String getRainType() {
		return rainType;
	}

	public void setRainType(String rainType) {
		this.rainType = rainType;
	}

	public String getName() {
		return name;
	}

	public RainItem setName(String name) {
		this.name = name;
		return this;
	}

	public Set<PinPoint> getPinpoints() {
		return pinpoints;
	}

	public void setPinpoints(Set<PinPoint> pinpoints) {
		this.pinpoints = pinpoints;
	}




}
