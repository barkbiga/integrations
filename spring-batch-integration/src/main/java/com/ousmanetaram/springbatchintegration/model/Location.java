package com.ousmanetaram.springbatchintegration.model;

public class Location {

	private String lon;

	private String lat;

	public Location(String lon, String lat) {
		super();
		this.lon = lon;
		this.lat = lat;
	}

	public String getLon() {
		return lon;
	}

	public void setLon(String lon) {
		this.lon = lon;
	}

	public String getLat() {
		return lat;
	}

	public void setLat(String lat) {
		this.lat = lat;
	}

}
