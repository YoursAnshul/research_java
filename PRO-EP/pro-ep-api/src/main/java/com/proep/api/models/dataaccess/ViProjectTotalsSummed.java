package com.proep.api.models.dataaccess;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "vi_ProjectTotalsSummed")
@IdClass(ViProjectTotalsSummedId.class)
public class ViProjectTotalsSummed {

	@Column(name = "total")
	private BigDecimal total;

	@Id
	@Column(name = "yearnumber")
	private Integer yearnumber;

	@Id
	@Column(name = "weeknumber")
	private Integer weeknumber;

	@Id
	@Column(name = "daynumber")
	private Integer daynumber;

	@Column(name = "scheduledate")
	private String scheduledate;

	@Column(name = "startdatetime")
	private OffsetDateTime startdatetime;

	public ViProjectTotalsSummed() {

	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
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

	public String getScheduledate() {
		return scheduledate;
	}

	public void setScheduledate(String scheduledate) {
		this.scheduledate = scheduledate;
	}

	public OffsetDateTime getStartdatetime() {
		return startdatetime;
	}

	public void setStartdatetime(OffsetDateTime startdatetime) {
		this.startdatetime = startdatetime;
	}

}
