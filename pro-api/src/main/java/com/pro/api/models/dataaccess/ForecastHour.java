package com.pro.api.models.dataaccess;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "ForecastHours") // Name of the database table
public class ForecastHour {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ForecastHoursID")
	private int forecastHoursId;

	@Column(name = "Projectid")
	private Integer projectId;

	@Column(name = "Month1")
	private LocalDate month1;

	@Column(name = "Forecasthours1")
	private Integer forecastHours1;

	@Column(name = "Comment1")
	private String comment1;

	@Column(name = "Weekchk1")
	private Boolean weekchk1;

	@Column(name = "WeekendChk1")
	private Boolean weekendChk1;

	@Column(name = "Freq1")
	private Integer freq1;

	@Column(name = "Month2")
	private LocalDate month2;

	@Column(name = "Forecasthours2")
	private Integer forecastHours2;

	@Column(name = "Comment2")
	private String comment2;

	@Column(name = "Weekchk2")
	private Boolean weekchk2;

	@Column(name = "WeekendChk2")
	private Boolean weekendChk2;

	@Column(name = "Freq2")
	private Integer freq2;

	@Column(name = "Month3")
	private LocalDate month3;

	@Column(name = "Forecasthours3")
	private Integer forecastHours3;

	@Column(name = "Comment3")
	private String comment3;

	@Column(name = "Weekchk3")
	private Boolean weekchk3;

	@Column(name = "WeekendChk3")
	private Boolean weekendChk3;

	@Column(name = "Freq3")
	private Integer freq3;

	@Column(name = "Month4")
	private LocalDate month4;

	@Column(name = "Forecasthours4")
	private Integer forecastHours4;

	@Column(name = "Comment4")
	private String comment4;

	@Column(name = "Weekchk4")
	private Boolean weekchk4;

	@Column(name = "WeekendChk4")
	private Boolean weekendChk4;

	@Column(name = "Freq4")
	private Integer freq4;

	@Column(name = "Month5")
	private LocalDate month5;

	@Column(name = "Forecasthours5")
	private Integer forecastHours5;

	@Column(name = "Comment5")
	private String comment5;

	@Column(name = "Weekchk5")
	private Boolean weekchk5;

	@Column(name = "WeekendChk5")
	private Boolean weekendChk5;

	@Column(name = "Freq5")
	private Integer freq5;

	@Column(name = "Month6")
	private LocalDate month6;

	@Column(name = "Forecasthours6")
	private Integer forecastHours6;

	@Column(name = "Comment6")
	private String comment6;

	@Column(name = "Weekchk6")
	private Boolean weekchk6;

	@Column(name = "WeekendChk6")
	private Boolean weekendChk6;

	@Column(name = "Freq6")
	private Integer freq6;

	@Column(name = "Month7")
	private LocalDate month7;

	@Column(name = "Forecasthours7")
	private Integer forecastHours7;

	@Column(name = "Comment7")
	private String comment7;

	@Column(name = "Weekchk7")
	private Boolean weekchk7;

	@Column(name = "WeekendChk7")
	private Boolean weekendChk7;

	@Column(name = "Freq7")
	private Integer freq7;

	@Column(name = "Month8")
	private LocalDate month8;

	@Column(name = "Forecasthours8")
	private Integer forecastHours8;

	@Column(name = "Comment8")
	private String comment8;

	@Column(name = "Weekchk8")
	private Boolean weekchk8;

	@Column(name = "WeekendChk8")
	private Boolean weekendChk8;

	@Column(name = "Freq8")
	private Integer freq8;

	@Column(name = "Month9")
	private LocalDate month9;

	@Column(name = "Forecasthours9")
	private Integer forecastHours9;

	@Column(name = "Comment9")
	private String comment9;

	@Column(name = "Weekchk9")
	private Boolean weekchk9;

	@Column(name = "WeekendChk9")
	private Boolean weekendChk9;

	@Column(name = "Freq9")
	private Integer freq9;

	@Column(name = "Month10")
	private LocalDate month10;

	@Column(name = "Forecasthours10")
	private Integer forecastHours10;

	@Column(name = "Comment10")
	private String comment10;

	@Column(name = "Weekchk10")
	private Boolean weekchk10;

	@Column(name = "WeekendChk10")
	private Boolean weekendChk10;

	@Column(name = "Freq10")
	private Integer freq10;

	@Column(name = "Month11")
	private LocalDate month11;

	@Column(name = "Forecasthours11")
	private Integer forecastHours11;

	@Column(name = "Comment11")
	private String comment11;

	@Column(name = "Weekchk11")
	private Boolean weekchk11;

	@Column(name = "WeekendChk11")
	private Boolean weekendChk11;

	@Column(name = "Freq11")
	private Integer freq11;

	@Column(name = "Month12")
	private LocalDate month12;

	@Column(name = "Forecasthours12")
	private Integer forecastHours12;

	@Column(name = "Comment12")
	private String comment12;

	@Column(name = "Weekchk12")
	private Boolean weekchk12;

	@Column(name = "WeekendChk12")
	private Boolean weekendChk12;

	@Column(name = "Freq12")
	private Integer freq12;

	@Column(name = "Month13")
	private LocalDate month13;

	@Column(name = "Forecasthours13")
	private Integer forecastHours13;

	@Column(name = "Comment13")
	private String comment13;

	@Column(name = "Weekchk13")
	private Boolean weekchk13;

	@Column(name = "WeekendChk13")
	private Boolean weekendChk13;

	@Column(name = "Freq13")
	private Integer freq13;

	@Column(name = "Month14")
	private LocalDate month14;

	@Column(name = "Forecasthours14")
	private Integer forecastHours14;

	@Column(name = "Comment14")
	private String comment14;

	@Column(name = "Weekchk14")
	private Boolean weekchk14;

	@Column(name = "WeekendChk14")
	private Boolean weekendChk14;

	@Column(name = "Freq14")
	private Integer freq14;

	@Column(name = "EntryBy")
	private String entryBy;

	@Column(name = "EntryDT")
	private LocalDate entryDt;

	@Column(name = "ModBy")
	private String modBy;

	@Column(name = "ModDt")
	private LocalDate modDt;

	public ForecastHour() {

	}

	public ForecastHour(int forecastHoursId, Integer projectId, LocalDate month1, LocalDate month2,
                        LocalDate month3, LocalDate month4, LocalDate month5, LocalDate month6,
                        LocalDate month7, LocalDate month8, LocalDate month9, LocalDate month10,
                        LocalDate month11, LocalDate month12, LocalDate month13, LocalDate month14, String entryBy,
                        LocalDate entryDt, String modBy, LocalDate modDt) {
		this.forecastHoursId = forecastHoursId;
		this.projectId = projectId;
		this.month1 = month1;
		this.month2 = month2;
		this.month3 = month3;
		this.month4 = month4;
		this.month5 = month5;
		this.month6 = month6;
		this.month7 = month7;
		this.month8 = month8;
		this.month9 = month9;
		this.month10 = month10;
		this.month11 = month11;
		this.month12 = month12;
		this.month13 = month13;
		this.month14 = month14;
		this.entryBy = entryBy;
		this.entryDt = entryDt;
		this.modBy = modBy;
		this.modDt = modDt;
	}

	public int getForecastHoursId() {
		return forecastHoursId;
	}

	public void setForecastHoursId(int forecastHoursId) {
		this.forecastHoursId = forecastHoursId;
	}

	public Integer getProjectId() {
		return projectId;
	}

	public void setProjectId(Integer projectId) {
		this.projectId = projectId;
	}

	public LocalDate getMonth1() {
		return month1;
	}

	public void setMonth1(LocalDate month1) {
		this.month1 = month1;
	}

	public Integer getForecastHours1() {
		return forecastHours1;
	}

	public void setForecastHours1(Integer forecastHours1) {
		this.forecastHours1 = forecastHours1;
	}

	public String getComment1() {
		return comment1;
	}

	public void setComment1(String comment1) {
		this.comment1 = comment1;
	}

	public Boolean getWeekchk1() {
		return weekchk1;
	}

	public void setWeekchk1(Boolean weekchk1) {
		this.weekchk1 = weekchk1;
	}

	public Boolean getWeekendChk1() {
		return weekendChk1;
	}

	public void setWeekendChk1(Boolean weekendChk1) {
		this.weekendChk1 = weekendChk1;
	}

	public Integer getFreq1() {
		return freq1;
	}

	public void setFreq1(Integer freq1) {
		this.freq1 = freq1;
	}

	public LocalDate getMonth2() {
		return month2;
	}

	public void setMonth2(LocalDate month2) {
		this.month2 = month2;
	}

	public Integer getForecastHours2() {
		return forecastHours2;
	}

	public void setForecastHours2(Integer forecastHours2) {
		this.forecastHours2 = forecastHours2;
	}

	public String getComment2() {
		return comment2;
	}

	public void setComment2(String comment2) {
		this.comment2 = comment2;
	}

	public Boolean getWeekchk2() {
		return weekchk2;
	}

	public void setWeekchk2(Boolean weekchk2) {
		this.weekchk2 = weekchk2;
	}

	public Boolean getWeekendChk2() {
		return weekendChk2;
	}

	public void setWeekendChk2(Boolean weekendChk2) {
		this.weekendChk2 = weekendChk2;
	}

	public Integer getFreq2() {
		return freq2;
	}

	public void setFreq2(Integer freq2) {
		this.freq2 = freq2;
	}

	public LocalDate getMonth3() {
		return month3;
	}

	public void setMonth3(LocalDate month3) {
		this.month3 = month3;
	}

	public Integer getForecastHours3() {
		return forecastHours3;
	}

	public void setForecastHours3(Integer forecastHours3) {
		this.forecastHours3 = forecastHours3;
	}

	public String getComment3() {
		return comment3;
	}

	public void setComment3(String comment3) {
		this.comment3 = comment3;
	}

	public Boolean getWeekchk3() {
		return weekchk3;
	}

	public void setWeekchk3(Boolean weekchk3) {
		this.weekchk3 = weekchk3;
	}

	public Boolean getWeekendChk3() {
		return weekendChk3;
	}

	public void setWeekendChk3(Boolean weekendChk3) {
		this.weekendChk3 = weekendChk3;
	}

	public Integer getFreq3() {
		return freq3;
	}

	public void setFreq3(Integer freq3) {
		this.freq3 = freq3;
	}

	public LocalDate getMonth4() {
		return month4;
	}

	public void setMonth4(LocalDate month4) {
		this.month4 = month4;
	}

	public Integer getForecastHours4() {
		return forecastHours4;
	}

	public void setForecastHours4(Integer forecastHours4) {
		this.forecastHours4 = forecastHours4;
	}

	public String getComment4() {
		return comment4;
	}

	public void setComment4(String comment4) {
		this.comment4 = comment4;
	}

	public Boolean getWeekchk4() {
		return weekchk4;
	}

	public void setWeekchk4(Boolean weekchk4) {
		this.weekchk4 = weekchk4;
	}

	public Boolean getWeekendChk4() {
		return weekendChk4;
	}

	public void setWeekendChk4(Boolean weekendChk4) {
		this.weekendChk4 = weekendChk4;
	}

	public Integer getFreq4() {
		return freq4;
	}

	public void setFreq4(Integer freq4) {
		this.freq4 = freq4;
	}

	public LocalDate getMonth5() {
		return month5;
	}

	public void setMonth5(LocalDate month5) {
		this.month5 = month5;
	}

	public Integer getForecastHours5() {
		return forecastHours5;
	}

	public void setForecastHours5(Integer forecastHours5) {
		this.forecastHours5 = forecastHours5;
	}

	public String getComment5() {
		return comment5;
	}

	public void setComment5(String comment5) {
		this.comment5 = comment5;
	}

	public Boolean getWeekchk5() {
		return weekchk5;
	}

	public void setWeekchk5(Boolean weekchk5) {
		this.weekchk5 = weekchk5;
	}

	public Boolean getWeekendChk5() {
		return weekendChk5;
	}

	public void setWeekendChk5(Boolean weekendChk5) {
		this.weekendChk5 = weekendChk5;
	}

	public Integer getFreq5() {
		return freq5;
	}

	public void setFreq5(Integer freq5) {
		this.freq5 = freq5;
	}

	public LocalDate getMonth6() {
		return month6;
	}

	public void setMonth6(LocalDate month6) {
		this.month6 = month6;
	}

	public Integer getForecastHours6() {
		return forecastHours6;
	}

	public void setForecastHours6(Integer forecastHours6) {
		this.forecastHours6 = forecastHours6;
	}

	public String getComment6() {
		return comment6;
	}

	public void setComment6(String comment6) {
		this.comment6 = comment6;
	}

	public Boolean getWeekchk6() {
		return weekchk6;
	}

	public void setWeekchk6(Boolean weekchk6) {
		this.weekchk6 = weekchk6;
	}

	public Boolean getWeekendChk6() {
		return weekendChk6;
	}

	public void setWeekendChk6(Boolean weekendChk6) {
		this.weekendChk6 = weekendChk6;
	}

	public Integer getFreq6() {
		return freq6;
	}

	public void setFreq6(Integer freq6) {
		this.freq6 = freq6;
	}

	public LocalDate getMonth7() {
		return month7;
	}

	public void setMonth7(LocalDate month7) {
		this.month7 = month7;
	}

	public Integer getForecastHours7() {
		return forecastHours7;
	}

	public void setForecastHours7(Integer forecastHours7) {
		this.forecastHours7 = forecastHours7;
	}

	public String getComment7() {
		return comment7;
	}

	public void setComment7(String comment7) {
		this.comment7 = comment7;
	}

	public Boolean getWeekchk7() {
		return weekchk7;
	}

	public void setWeekchk7(Boolean weekchk7) {
		this.weekchk7 = weekchk7;
	}

	public Boolean getWeekendChk7() {
		return weekendChk7;
	}

	public void setWeekendChk7(Boolean weekendChk7) {
		this.weekendChk7 = weekendChk7;
	}

	public Integer getFreq7() {
		return freq7;
	}

	public void setFreq7(Integer freq7) {
		this.freq7 = freq7;
	}

	public LocalDate getMonth8() {
		return month8;
	}

	public void setMonth8(LocalDate month8) {
		this.month8 = month8;
	}

	public Integer getForecastHours8() {
		return forecastHours8;
	}

	public void setForecastHours8(Integer forecastHours8) {
		this.forecastHours8 = forecastHours8;
	}

	public String getComment8() {
		return comment8;
	}

	public void setComment8(String comment8) {
		this.comment8 = comment8;
	}

	public Boolean getWeekchk8() {
		return weekchk8;
	}

	public void setWeekchk8(Boolean weekchk8) {
		this.weekchk8 = weekchk8;
	}

	public Boolean getWeekendChk8() {
		return weekendChk8;
	}

	public void setWeekendChk8(Boolean weekendChk8) {
		this.weekendChk8 = weekendChk8;
	}

	public Integer getFreq8() {
		return freq8;
	}

	public void setFreq8(Integer freq8) {
		this.freq8 = freq8;
	}

	public LocalDate getMonth9() {
		return month9;
	}

	public void setMonth9(LocalDate month9) {
		this.month9 = month9;
	}

	public Integer getForecastHours9() {
		return forecastHours9;
	}

	public void setForecastHours9(Integer forecastHours9) {
		this.forecastHours9 = forecastHours9;
	}

	public String getComment9() {
		return comment9;
	}

	public void setComment9(String comment9) {
		this.comment9 = comment9;
	}

	public Boolean getWeekchk9() {
		return weekchk9;
	}

	public void setWeekchk9(Boolean weekchk9) {
		this.weekchk9 = weekchk9;
	}

	public Boolean getWeekendChk9() {
		return weekendChk9;
	}

	public void setWeekendChk9(Boolean weekendChk9) {
		this.weekendChk9 = weekendChk9;
	}

	public Integer getFreq9() {
		return freq9;
	}

	public void setFreq9(Integer freq9) {
		this.freq9 = freq9;
	}

	public LocalDate getMonth10() {
		return month10;
	}

	public void setMonth10(LocalDate month10) {
		this.month10 = month10;
	}

	public Integer getForecastHours10() {
		return forecastHours10;
	}

	public void setForecastHours10(Integer forecastHours10) {
		this.forecastHours10 = forecastHours10;
	}

	public String getComment10() {
		return comment10;
	}

	public void setComment10(String comment10) {
		this.comment10 = comment10;
	}

	public Boolean getWeekchk10() {
		return weekchk10;
	}

	public void setWeekchk10(Boolean weekchk10) {
		this.weekchk10 = weekchk10;
	}

	public Boolean getWeekendChk10() {
		return weekendChk10;
	}

	public void setWeekendChk10(Boolean weekendChk10) {
		this.weekendChk10 = weekendChk10;
	}

	public Integer getFreq10() {
		return freq10;
	}

	public void setFreq10(Integer freq10) {
		this.freq10 = freq10;
	}

	public LocalDate getMonth11() {
		return month11;
	}

	public void setMonth11(LocalDate month11) {
		this.month11 = month11;
	}

	public Integer getForecastHours11() {
		return forecastHours11;
	}

	public void setForecastHours11(Integer forecastHours11) {
		this.forecastHours11 = forecastHours11;
	}

	public String getComment11() {
		return comment11;
	}

	public void setComment11(String comment11) {
		this.comment11 = comment11;
	}

	public Boolean getWeekchk11() {
		return weekchk11;
	}

	public void setWeekchk11(Boolean weekchk11) {
		this.weekchk11 = weekchk11;
	}

	public Boolean getWeekendChk11() {
		return weekendChk11;
	}

	public void setWeekendChk11(Boolean weekendChk11) {
		this.weekendChk11 = weekendChk11;
	}

	public Integer getFreq11() {
		return freq11;
	}

	public void setFreq11(Integer freq11) {
		this.freq11 = freq11;
	}

	public LocalDate getMonth12() {
		return month12;
	}

	public void setMonth12(LocalDate month12) {
		this.month12 = month12;
	}

	public Integer getForecastHours12() {
		return forecastHours12;
	}

	public void setForecastHours12(Integer forecastHours12) {
		this.forecastHours12 = forecastHours12;
	}

	public String getComment12() {
		return comment12;
	}

	public void setComment12(String comment12) {
		this.comment12 = comment12;
	}

	public Boolean getWeekchk12() {
		return weekchk12;
	}

	public void setWeekchk12(Boolean weekchk12) {
		this.weekchk12 = weekchk12;
	}

	public Boolean getWeekendChk12() {
		return weekendChk12;
	}

	public void setWeekendChk12(Boolean weekendChk12) {
		this.weekendChk12 = weekendChk12;
	}

	public Integer getFreq12() {
		return freq12;
	}

	public void setFreq12(Integer freq12) {
		this.freq12 = freq12;
	}

	public LocalDate getMonth13() {
		return month13;
	}

	public void setMonth13(LocalDate month13) {
		this.month13 = month13;
	}

	public Integer getForecastHours13() {
		return forecastHours13;
	}

	public void setForecastHours13(Integer forecastHours13) {
		this.forecastHours13 = forecastHours13;
	}

	public String getComment13() {
		return comment13;
	}

	public void setComment13(String comment13) {
		this.comment13 = comment13;
	}

	public Boolean getWeekchk13() {
		return weekchk13;
	}

	public void setWeekchk13(Boolean weekchk13) {
		this.weekchk13 = weekchk13;
	}

	public Boolean getWeekendChk13() {
		return weekendChk13;
	}

	public void setWeekendChk13(Boolean weekendChk13) {
		this.weekendChk13 = weekendChk13;
	}

	public Integer getFreq13() {
		return freq13;
	}

	public void setFreq13(Integer freq13) {
		this.freq13 = freq13;
	}

	public LocalDate getMonth14() {
		return month14;
	}

	public void setMonth14(LocalDate month14) {
		this.month14 = month14;
	}

	public Integer getForecastHours14() {
		return forecastHours14;
	}

	public void setForecastHours14(Integer forecastHours14) {
		this.forecastHours14 = forecastHours14;
	}

	public String getComment14() {
		return comment14;
	}

	public void setComment14(String comment14) {
		this.comment14 = comment14;
	}

	public Boolean getWeekchk14() {
		return weekchk14;
	}

	public void setWeekchk14(Boolean weekchk14) {
		this.weekchk14 = weekchk14;
	}

	public Boolean getWeekendChk14() {
		return weekendChk14;
	}

	public void setWeekendChk14(Boolean weekendChk14) {
		this.weekendChk14 = weekendChk14;
	}

	public Integer getFreq14() {
		return freq14;
	}

	public void setFreq14(Integer freq14) {
		this.freq14 = freq14;
	}

	public String getEntryBy() {
		return entryBy;
	}

	public void setEntryBy(String entryBy) {
		this.entryBy = entryBy;
	}

	public LocalDate getEntryDt() {
		return entryDt;
	}

	public void setEntryDt(LocalDate entryDt) {
		this.entryDt = entryDt;
	}

	public String getModBy() {
		return modBy;
	}

	public void setModBy(String modBy) {
		this.modBy = modBy;
	}

	public LocalDate getModDt() {
		return modDt;
	}

	public void setModDt(LocalDate modDt) {
		this.modDt = modDt;
	}

}
