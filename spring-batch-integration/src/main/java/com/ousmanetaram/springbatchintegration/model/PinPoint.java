package com.ousmanetaram.springbatchintegration.model;

import java.io.Serializable;
import java.time.LocalDateTime;


public class PinPoint implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	private double rainValue;
    
	private String rainEvent;
    
	private LocalDateTime eventDate;

	private Location location;

	public PinPoint() {
		super();
	}

	public double getRainValue() {
		return rainValue;
	}

	public void setRainValue(double rainValue) {
		this.rainValue = rainValue;
	}

	public String getRainEvent() {
		return rainEvent;
	}

	public void setRainEvent(String rainEvent) {
		this.rainEvent = rainEvent;
	}

	public LocalDateTime getEventDate() {
		return eventDate;
	}

	public void setEventDate(LocalDateTime eventDate) {
		this.eventDate = eventDate;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

    

}
