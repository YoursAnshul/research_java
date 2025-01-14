package com.proep.api.models.dataaccess;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "BlockOutDates")
public class BlockOutDate {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "BlockOutDateId")
	private int blockOutDateId;

	@Column(name = "BlockOutDay")
	private OffsetDateTime blockOutDay;

	@Column(name = "AllDay")
	private Boolean allDay;

	@Column(name = "StartTime")
	private String startTime;

	@Column(name = "EndTime")
	private String endTime;

	public BlockOutDate() {
    }
	
	public int getBlockOutDateId() {
		return blockOutDateId;
	}

	public void setBlockOutDateId(int blockOutDateId) {
		this.blockOutDateId = blockOutDateId;
	}

	public OffsetDateTime getBlockOutDay() {
		return blockOutDay;
	}

	public void setBlockOutDay(OffsetDateTime blockOutDay) {
		this.blockOutDay = blockOutDay;
	}

	public Boolean getAllDay() {
		return allDay;
	}

	public void setAllDay(Boolean allDay) {
		this.allDay = allDay;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	
}
