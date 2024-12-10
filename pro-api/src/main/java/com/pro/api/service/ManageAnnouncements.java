package com.pro.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pro.api.controllers.GeneralResponse;
import com.pro.api.response.AnnouncementResponse;
import com.pro.api.response.AuthorResponse;
import com.pro.api.response.ProjectResponse;

@Service
public interface ManageAnnouncements {

	public List<AuthorResponse> getAllAuthors();

	public List<ProjectResponse> getAllProjects();

	public GeneralResponse saveAnnouncement(AnnouncementResponse request);
}
