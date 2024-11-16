package com.pro.api.models.dataaccess;

import jakarta.persistence.*;

@Entity
@Table(name = "TimeCodes")
public class TimeCode {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "TimeCodeValue")
	private int timeCodeValue;
	
	@Column(name = "TimePeriod")
	private String timePeriod;

	@Column(name = "TimePeriodampm")
	private String timePeriodampm;

	public TimeCode() {
		
	}

	public String getTimePeriod() {
		return timePeriod;
	}

	public void setTimePeriod(String timePeriod) {
		this.timePeriod = timePeriod;
	}

	public int getTimeCodeValue() {
		return timeCodeValue;
	}

	public void setTimeCodeValue(int timeCodeValue) {
		this.timeCodeValue = timeCodeValue;
	}

	public String getTimePeriodampm() {
		return timePeriodampm;
	}

	public void setTimePeriodampm(String timePeriodampm) {
		this.timePeriodampm = timePeriodampm;
	}
}
