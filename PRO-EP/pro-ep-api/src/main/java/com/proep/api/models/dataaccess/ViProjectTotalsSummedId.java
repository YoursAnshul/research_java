package com.proep.api.models.dataaccess;

import java.io.Serializable;

public class ViProjectTotalsSummedId implements Serializable {
    private Integer yearnumber;
    private Integer weeknumber;
    private Integer daynumber;

    public ViProjectTotalsSummedId() {
    	
    }

	public Integer getYearnumber() {
		return yearnumber;
	}

	public void setYearnumber(Integer yearnumber) {
		this.yearnumber = yearnumber;
	}

	public Integer getWeeknumber() {
		return weeknumber;
	}

	public void setWeeknumber(Integer weeknumber) {
		this.weeknumber = weeknumber;
	}

	public Integer getDaynumber() {
		return daynumber;
	}

	public void setDaynumber(Integer daynumber) {
		this.daynumber = daynumber;
	}
    
}
