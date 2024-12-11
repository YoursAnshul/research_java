package com.pro.api.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.pro.api.controllers.GeneralResponse;
import com.pro.api.response.AnnouncementResponse;
import com.pro.api.response.AuthorResponse;
import com.pro.api.response.ProjectResponse;
import com.pro.api.service.ManageAnnouncements;

@Service
public class ManageAnnouncementsImpl implements ManageAnnouncements {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public List<AuthorResponse> getAllAuthors() {
		StringBuilder sql = new StringBuilder();
		sql.append(" SELECT userid, CONCAT(fname, ' ', lname) AS userName  ");
		sql.append(" FROM core.users  ");
		sql.append(" ORDER BY userid ASC ");
		List<AuthorResponse> list = this.jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
			AuthorResponse obj = new AuthorResponse();
			obj.setUserId(rs.getLong("userid"));
			obj.setUserName(rs.getString("userName"));
			return obj;
		});
		return list;
	}

	@Override
	public List<ProjectResponse> getAllProjects() {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT projectid, projectname, projectcolor ");
		sql.append("FROM core.projects WHERE active = 1 AND projectType <> 4 ORDER BY projectid ");

		List<ProjectResponse> projects = this.jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
			ProjectResponse project = new ProjectResponse();
			project.setProjectId(rs.getLong("projectid"));
			project.setProjectName(rs.getString("projectname"));
			project.setProjectColor(rs.getString("projectcolor"));
			return project;
		});

		return projects;
	}

	@Override
	public GeneralResponse saveAnnouncement(AnnouncementResponse request) {
		if (request.getTitle() == null || request.getTitle().isEmpty()) {
			throw new IllegalArgumentException("Title cannot be null or empty");
		}
		if (request.getStartDate() == null) {
			throw new IllegalArgumentException("Start date cannot be null");
		}

		String projectIds = (request.getProjectIds() != null && !request.getProjectIds().isEmpty())
				? request.getProjectIds().stream().map(String::valueOf).collect(Collectors.joining("|"))
				: null;
		System.err.println("projectIds---" + projectIds);

		String sql = " INSERT INTO core.announcements (titletext, bodytext, author, dispauthor, "
				+ " startdate, expiredate, dispprojects) VALUES (?, ?, ?, ?, ?, ?, ?)";

		int rowsAffected = this.jdbcTemplate.update(sql, request.getTitle(), request.getBodyText(),
				request.getAuthorId(), request.getIsAuthor(), request.getStartDate(), request.getExpireDate(),
				projectIds);

		GeneralResponse response = new GeneralResponse();
		if (rowsAffected > 0) {
			response.Message = "Announcement saved successfully!";
		} else {
			response.Message = "Failed to save the announcement. Please try again.";
		}

		return response;
	}

}
