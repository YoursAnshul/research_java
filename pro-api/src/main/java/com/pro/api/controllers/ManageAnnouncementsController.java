package com.pro.api.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pro.api.response.AnnouncementResponse;
import com.pro.api.response.AuthorResponse;
import com.pro.api.response.PageResponse;
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

	@GetMapping("/announcement/{id}")
	public ResponseEntity<GeneralResponse> getAnnouncement(@PathVariable Long id) {
		GeneralResponse announcement = manageAnnouncements.getAnnouncement(id);
		return ResponseEntity.status(HttpStatus.OK).body(announcement);
	}

	@GetMapping("/list/{page}")
	public ResponseEntity<PageResponse<AnnouncementResponse>> getAnnouncementList(@PathVariable Integer page,
			@RequestParam(required = false, value = "sortBy") String sortBy,
			@RequestParam(required = false, value = "orderBy") String orderBy,
			@RequestParam(required = false, value = "limit") Integer limit,
			@RequestParam(required = false, value = "keyword") String keyword,
			@RequestParam(required = false, value = "authorName") String authorName) {
		int offset = 0;
		if (limit == null) {
			limit = 10;
		}
		if (page > 0) {
			offset = (page - 1) * limit;
		}
		PageResponse<AnnouncementResponse> response = manageAnnouncements.getList(sortBy, orderBy, limit, offset,
				keyword, authorName);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@GetMapping("/user")
	public ResponseEntity<AuthorResponse> getLoginUser(@RequestParam(required = false, value = "email") String email) {
		AuthorResponse user = manageAnnouncements.getLoginUser(email);
		return ResponseEntity.status(HttpStatus.OK).body(user);
	}

	@DeleteMapping("/{id}/{user}")
	public ResponseEntity<GeneralResponse> delete(@PathVariable Integer id,@PathVariable String user) {
		GeneralResponse res = manageAnnouncements.delete(id,user);
		return ResponseEntity.status(HttpStatus.OK).body(res);
	}

	@GetMapping("/all-authors")
	public ResponseEntity<GeneralResponse> getAuthorList() {
		List<AuthorResponse> authors = manageAnnouncements.getAuthors();
		GeneralResponse response = new GeneralResponse();
		response.Subject = authors;
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@GetMapping("/list")
	public ResponseEntity<GeneralResponse> getAouncementList() {
		GeneralResponse announcementList = manageAnnouncements.getAnnouncementList();
		return ResponseEntity.status(HttpStatus.OK).body(announcementList);
	}
}
