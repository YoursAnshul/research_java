package com.proep.api.models.business;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.proep.api.models.dataaccess.CommunicationsHub;

public class CommunicationsHubEntry {
	private List<CommunicationsHub> fieldValues;
	private String entryBy;
	private LocalDateTime entryDt;

	public CommunicationsHubEntry() {
	}

	public CommunicationsHubEntry(List<CommunicationsHub> fieldValues) {
		this.fieldValues = fieldValues;
		if (this.fieldValues != null && !this.fieldValues.isEmpty()) {
			Optional<CommunicationsHub> entryBy = this.fieldValues.stream().filter(f -> f.getEntryBy() != null)
					.findFirst();
			if (entryBy.isPresent()) {
				this.entryBy = entryBy.get().getEntryBy();
			}
			Optional<CommunicationsHub> entryDt = this.fieldValues.stream().filter(f -> f.getEntryDt() != null)
					.findFirst();
			if (entryDt.isPresent()) {
				this.entryDt = entryDt.get().getEntryDt();
			}
		}
	}

	public List<CommunicationsHub> getFieldValues() {
		return fieldValues;
	}

	public void setFieldValues(List<CommunicationsHub> fieldValues) {
		this.fieldValues = fieldValues;
	}

	public String getEntryBy() {
		return entryBy;
	}

	public void setEntryBy(String entryBy) {
		this.entryBy = entryBy;
	}

	public LocalDateTime getEntryDt() {
		return entryDt;
	}

	public void setEntryDt(LocalDateTime entryDt) {
		this.entryDt = entryDt;
	}
}
