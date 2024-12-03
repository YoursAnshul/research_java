package com.pro.api.models.dataaccess;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "CoreHours")
public class CoreHour {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "CoreHoursID")
	private int coreHoursId;

	@Column(name = "Dempoid")
	private String dempoid;

	@Column(name = "Month1")
	private LocalDate month1;

	@Column(name = "Corehours1")
	private Integer coreHours1;

	@Column(name = "Month2")
	private LocalDate month2;

	@Column(name = "Corehours2")
	private Integer coreHours2;

	@Column(name = "Month3")
	private LocalDate month3;

	@Column(name = "Corehours3")
	private Integer coreHours3;

	@Column(name = "Month4")
	private LocalDate month4;

	@Column(name = "Corehours4")
	private Integer coreHours4;

	@Column(name = "Month5")
	private LocalDate month5;

	@Column(name = "Corehours5")
	private Integer coreHours5;

	@Column(name = "Month6")
	private LocalDate month6;

	@Column(name = "Corehours6")
	private Integer coreHours6;

	@Column(name = "Month7")
	private LocalDate month7;

	@Column(name = "Corehours7")
	private Integer coreHours7;

	@Column(name = "Month8")
	private LocalDate month8;

	@Column(name = "Corehours8")
	private Integer coreHours8;

	@Column(name = "Month9")
	private LocalDate month9;

	@Column(name = "Corehours9")
	private Integer coreHours9;

	@Column(name = "Month10")
	private LocalDate month10;

	@Column(name = "Corehours10")
	private Integer coreHours10;

	@Column(name = "Month11")
	private LocalDate month11;

	@Column(name = "Corehours11")
	private Integer coreHours11;

	@Column(name = "Month12")
	private LocalDate month12;

	@Column(name = "Corehours12")
	private Integer coreHours12;

	@Column(name = "Month13")
	private LocalDate month13;

	@Column(name = "Corehours13")
	private Integer coreHours13;

	@Column(name = "Month14")
	private LocalDate month14;

	@Column(name = "Corehours14")
	private Integer coreHours14;

	@Column(name = "EntryBy")
	private String entryBy;

	@Column(name = "EntryDT")
	private LocalDate entryDt;

	@Column(name = "ModBy")
	private String modBy;

	@Column(name = "ModDt")
	private LocalDate modDt;

	// Constructors, getters, and setters

	public CoreHour() {
	}

	public CoreHour(int coreHoursId, String dempoid, Integer coreHours1, Integer coreHours2, Integer coreHours3,
                    Integer coreHours4, Integer coreHours5, Integer coreHours6, Integer coreHours7, Integer coreHours8,
                    Integer coreHours9, Integer coreHours10, Integer coreHours11, Integer coreHours12, Integer coreHours13,
                    Integer coreHours14, LocalDate month1, LocalDate month2, LocalDate month3, LocalDate month4,
                    LocalDate month5, LocalDate month6, LocalDate month7, LocalDate month8, LocalDate month9, LocalDate month10,
                    LocalDate month11, LocalDate month12, LocalDate month13, LocalDate month14, String entryBy,
                    LocalDate entryDt, String modBy, LocalDate modDt) {
		this.coreHoursId = coreHoursId;
		this.dempoid = dempoid;
		this.coreHours1 = coreHours1;
		this.coreHours2 = coreHours2;
		this.coreHours3 = coreHours3;
		this.coreHours4 = coreHours4;
		this.coreHours5 = coreHours5;
		this.coreHours6 = coreHours6;
		this.coreHours7 = coreHours7;
		this.coreHours8 = coreHours8;
		this.coreHours9 = coreHours9;
		this.coreHours10 = coreHours10;
		this.coreHours11 = coreHours11;
		this.coreHours12 = coreHours12;
		this.coreHours13 = coreHours13;
		this.coreHours14 = coreHours14;
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

	public int getCoreHoursId() {
		return coreHoursId;
	}

	public void setCoreHoursId(int coreHoursId) {
		this.coreHoursId = coreHoursId;
	}

	public String getDempoid() {
		return dempoid;
	}

	public void setDempoid(String dempoid) {
		this.dempoid = dempoid;
	}

	public LocalDate getMonth1() {
		return month1;
	}

	public void setMonth1(LocalDate month1) {
		this.month1 = month1;
	}

	public Integer getCoreHours1() {
		return coreHours1;
	}

	public void setCoreHours1(Integer coreHours1) {
		this.coreHours1 = coreHours1;
	}

	public LocalDate getMonth2() {
		return month2;
	}

	public void setMonth2(LocalDate month2) {
		this.month2 = month2;
	}

	public Integer getCoreHours2() {
		return coreHours2;
	}

	public void setCoreHours2(Integer coreHours2) {
		this.coreHours2 = coreHours2;
	}

	public LocalDate getMonth3() {
		return month3;
	}

	public void setMonth3(LocalDate month3) {
		this.month3 = month3;
	}

	public Integer getCoreHours3() {
		return coreHours3;
	}

	public void setCoreHours3(Integer coreHours3) {
		this.coreHours3 = coreHours3;
	}

	public LocalDate getMonth4() {
		return month4;
	}

	public void setMonth4(LocalDate month4) {
		this.month4 = month4;
	}

	public Integer getCoreHours4() {
		return coreHours4;
	}

	public void setCoreHours4(Integer coreHours4) {
		this.coreHours4 = coreHours4;
	}

	public LocalDate getMonth5() {
		return month5;
	}

	public void setMonth5(LocalDate month5) {
		this.month5 = month5;
	}

	public Integer getCoreHours5() {
		return coreHours5;
	}

	public void setCoreHours5(Integer coreHours5) {
		this.coreHours5 = coreHours5;
	}

	public LocalDate getMonth6() {
		return month6;
	}

	public void setMonth6(LocalDate month6) {
		this.month6 = month6;
	}

	public Integer getCoreHours6() {
		return coreHours6;
	}

	public void setCoreHours6(Integer coreHours6) {
		this.coreHours6 = coreHours6;
	}

	public LocalDate getMonth7() {
		return month7;
	}

	public void setMonth7(LocalDate month7) {
		this.month7 = month7;
	}

	public Integer getCoreHours7() {
		return coreHours7;
	}

	public void setCoreHours7(Integer coreHours7) {
		this.coreHours7 = coreHours7;
	}

	public LocalDate getMonth8() {
		return month8;
	}

	public void setMonth8(LocalDate month8) {
		this.month8 = month8;
	}

	public Integer getCoreHours8() {
		return coreHours8;
	}

	public void setCoreHours8(Integer coreHours8) {
		this.coreHours8 = coreHours8;
	}

	public LocalDate getMonth9() {
		return month9;
	}

	public void setMonth9(LocalDate month9) {
		this.month9 = month9;
	}

	public Integer getCoreHours9() {
		return coreHours9;
	}

	public void setCoreHours9(Integer coreHours9) {
		this.coreHours9 = coreHours9;
	}

	public LocalDate getMonth10() {
		return month10;
	}

	public void setMonth10(LocalDate month10) {
		this.month10 = month10;
	}

	public Integer getCoreHours10() {
		return coreHours10;
	}

	public void setCoreHours10(Integer coreHours10) {
		this.coreHours10 = coreHours10;
	}

	public LocalDate getMonth11() {
		return month11;
	}

	public void setMonth11(LocalDate month11) {
		this.month11 = month11;
	}

	public Integer getCoreHours11() {
		return coreHours11;
	}

	public void setCoreHours11(Integer coreHours11) {
		this.coreHours11 = coreHours11;
	}

	public LocalDate getMonth12() {
		return month12;
	}

	public void setMonth12(LocalDate month12) {
		this.month12 = month12;
	}

	public Integer getCoreHours12() {
		return coreHours12;
	}

	public void setCoreHours12(Integer coreHours12) {
		this.coreHours12 = coreHours12;
	}

	public LocalDate getMonth13() {
		return month13;
	}

	public void setMonth13(LocalDate month13) {
		this.month13 = month13;
	}

	public Integer getCoreHours13() {
		return coreHours13;
	}

	public void setCoreHours13(Integer coreHours13) {
		this.coreHours13 = coreHours13;
	}

	public LocalDate getMonth14() {
		return month14;
	}

	public void setMonth14(LocalDate month14) {
		this.month14 = month14;
	}

	public Integer getCoreHours14() {
		return coreHours14;
	}

	public void setCoreHours14(Integer coreHours14) {
		this.coreHours14 = coreHours14;
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
