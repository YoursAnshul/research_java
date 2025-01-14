package com.proep.api.models.business;

import java.time.LocalDate;
import java.util.List;

import com.proep.api.models.dataaccess.Schedule;
import com.proep.api.models.dataaccess.ValidationMessage;

public class ValidationMessagePlus {
    private int validationMessagesId;
    private String dempoId;
    private int messageId;
    private String messageText;
    private LocalDate inMonth;
    private String scheduleKeys;
    private String details;
    private List<Schedule> schedules;

    public ValidationMessagePlus() {
        // Default constructor
    }

    public ValidationMessagePlus(ValidationMessage validationMessage, String messageText) {
        this.validationMessagesId = validationMessage.getValidationMessagesId();
        this.dempoId = validationMessage.getDempoId();
        this.messageId = validationMessage.getMessageId();
        this.inMonth = validationMessage.getInMonth();
        this.details = validationMessage.getDetails();
        this.scheduleKeys = validationMessage.getScheduleKeys();
        this.messageText = messageText;
    }

    // Getters and Setters

    public int getValidationMessagesId() {
        return validationMessagesId;
    }

    public void setValidationMessagesId(int validationMessagesId) {
        this.validationMessagesId = validationMessagesId;
    }

    public String getDempoId() {
        return dempoId;
    }

    public void setDempoId(String dempoId) {
        this.dempoId = dempoId;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public LocalDate getInMonth() {
        return inMonth;
    }

    public void setInMonth(LocalDate inMonth) {
        this.inMonth = inMonth;
    }

    public String getScheduleKeys() {
        return scheduleKeys;
    }

    public void setScheduleKeys(String scheduleKeys) {
        this.scheduleKeys = scheduleKeys;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public List<Schedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<Schedule> schedules) {
        this.schedules = schedules;
    }
}
