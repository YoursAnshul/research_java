package com.proep.api.models.dataaccess;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "InterviewerTimeCards")
public class InterviewerTimeCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TimeCardID")
    private int timeCardId;

    @Column(name = "dempoid")
    private String dempoid;

    @Column(name = "WorkDate")
    private LocalDate workDate;

    @Column(name = "EntryBy")
    private String entryBy;

    @Column(name = "EntryDt")
    private LocalDateTime entryDt;

    @Column(name = "ModBy")
    private String modBy;

    @Column(name = "ModDt")
    private LocalDateTime modDt;

    @Column(name = "MachineName")
    private String machineName;

    @Column(name = "datetimein")
    private OffsetDateTime datetimein;

    @Column(name = "datetimeout")
    private OffsetDateTime datetimeout;

    @Column(name = "ExternalIP")
    private String externalIp;

    public InterviewerTimeCard() {

    }

    public int getTimeCardId() {
        return timeCardId;
    }

    public void setTimeCardId(int timeCardId) {
        this.timeCardId = timeCardId;
    }

    public String getDempoid() {
        return dempoid;
    }

    public void setDempoid(String dempoid) {
        this.dempoid = dempoid;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
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

    public String getModBy() {
        return modBy;
    }

    public void setModBy(String modBy) {
        this.modBy = modBy;
    }

    public LocalDateTime getModDt() {
        return modDt;
    }

    public void setModDt(LocalDateTime modDt) {
        this.modDt = modDt;
    }

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public OffsetDateTime getDatetimein() {
        return datetimein;
    }

    public void setDatetimein(OffsetDateTime datetimein) {
        this.datetimein = datetimein;
    }

    public OffsetDateTime getDatetimeout() {
        return datetimeout;
    }

    public void setDatetimeout(OffsetDateTime datetimeout) {
        this.datetimeout = datetimeout;
    }

    public String getExternalIp() {
        return externalIp;
    }

    public void setExternalIp(String externalIp) {
        this.externalIp = externalIp;
    }
}
