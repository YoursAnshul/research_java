package com.pro.api.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pro.api.response.AnnouncementResponse;
import com.pro.api.response.AuthorResponse;
import com.pro.api.response.ProjectResponse;
import com.pro.api.service.ManageAnnouncements;

@RestController
@RequestMapping("/manage-announement")
public class ManageAnnouncementsController {

	@Autowired
	private ManageAnnouncements manageAnnouncements;

	@GetMapping("/authors")
	public ResponseEntity<List<AuthorResponse>> getAuthors() {
		List<AuthorResponse> allAuthors = manageAnnouncements.getAllAuthors();
		return ResponseEntity.status(HttpStatus.OK).body(allAuthors);
	}

	@GetMapping("/projects")
	public ResponseEntity<List<ProjectResponse>> getProjects() {
		List<ProjectResponse> allProjects = manageAnnouncements.getAllProjects();
		return ResponseEntity.status(HttpStatus.OK).body(allProjects);
	}

	@PostMapping("/save")
	public ResponseEntity<GeneralResponse> saveAnnouncement(@RequestBody AnnouncementResponse request) {
		GeneralResponse response = manageAnnouncements.saveAnnouncement(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

}
