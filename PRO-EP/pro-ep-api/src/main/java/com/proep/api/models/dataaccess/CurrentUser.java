package com.proep.api.models.dataaccess;

import java.util.List;


public class CurrentUser {
    private User user;
    private List<InterviewerTimeCard> timecards;

    public CurrentUser() {

    }

    public CurrentUser(User user, List<InterviewerTimeCard> timecards) {
		this.user = user;
		this.timecards = timecards;
	}

	public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<InterviewerTimeCard> getTimecards() {
        return timecards;
    }

    public void setTimecards(List<InterviewerTimeCard> timecards) {
        this.timecards = timecards;
    }
}