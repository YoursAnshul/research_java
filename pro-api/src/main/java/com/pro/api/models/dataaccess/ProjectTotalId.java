package com.pro.api.models.dataaccess;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class ProjectTotalId implements Serializable {
    private Integer projectId;
    private Integer dayNumber;

    // default constructor
    public ProjectTotalId() {}

    public ProjectTotalId(Integer projectId, Integer dayNumber) {
        this.projectId = projectId;
        this.dayNumber = dayNumber;
    }

    // getters, setters, equals() and hashCode() methods
    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Integer getDayNumber() {
        return dayNumber;
    }

    public void setDayNumber(Integer dayNumber) {
        this.dayNumber = dayNumber;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((projectId == null) ? 0 : projectId.hashCode());
        result = prime * result + ((dayNumber == null) ? 0 : dayNumber.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProjectTotalId other = (ProjectTotalId) obj;
        if (projectId == null) {
            if (other.projectId != null)
                return false;
        } else if (!projectId.equals(other.projectId))
            return false;
        if (dayNumber == null) {
            if (other.dayNumber != null)
                return false;
        } else if (!dayNumber.equals(other.dayNumber))
            return false;
        return true;
    }

}
