package com.pro.api.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.pro.api.controllers.GeneralResponse;
import com.pro.api.response.AnnouncementResponse;
import com.pro.api.response.AuthorResponse;
import com.pro.api.response.PageResponse;
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
		String sql = " INSERT INTO core.announcements (titletext, bodytext, author, dispauthor, "
				+ " startdate, expiredate, dispprojects, icon) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

		int rowsAffected = this.jdbcTemplate.update(sql, request.getTitle(), request.getBodyText(),
				request.getAuthorId(), request.getIsAuthor(), request.getStartDate(), request.getExpireDate(),
				projectIds, request.getIcon());

		GeneralResponse response = new GeneralResponse();
		if (rowsAffected > 0) {
			response.Message = "Announcement saved successfully!";
		} else {
			response.Message = "Failed to save the announcement. Please try again.";
		}

		return response;
	}

	@Override
	public GeneralResponse getAnnouncement(Long id) {
		GeneralResponse response = new GeneralResponse();
		AnnouncementResponse res = new AnnouncementResponse();

		String sql = "SELECT  a.announcementid, a.icon, a.titletext, a.bodytext, a.author, a.dispauthor, a.startdate, a.expiredate, a.dispprojects "
				+ " FROM core.announcements a WHERE announcementid = " + id + "";

		try {
			res = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
				AnnouncementResponse announcement = new AnnouncementResponse();
				announcement.setAnnouncementId(rs.getLong("announcementid"));
				announcement.setTitle(rs.getString("titletext"));
				announcement.setIcon(rs.getString("icon"));
				announcement.setBodyText(rs.getString("bodytext"));
				announcement.setAuthorId(rs.getLong("author"));
				announcement.setIsAuthor(rs.getBoolean("dispauthor"));
				announcement.setStartDate(rs.getDate("startdate"));
				announcement.setExpireDate(rs.getDate("expiredate"));
				String projectIds = rs.getString("dispprojects");
				if (projectIds != null && !projectIds.isEmpty()) {
					List<Long> projectIdList = Arrays.stream(projectIds.split("\\|")).map(Long::valueOf)
							.collect(Collectors.toList());
					announcement.setProjectIds(projectIdList);
				}
				return announcement;
			});

			response.Subject = res;
			response.Message = "Announcement fetched successfully!";

		} catch (EmptyResultDataAccessException e) {
			response.Message = "Announcement not found for the provided ID.";
			e.printStackTrace();
		} catch (Exception e) {
			response.Message = "An error occurred while fetching the announcement: " + e.getMessage();
			e.printStackTrace();
		}

		return response;
	}

	@Override
	public PageResponse<AnnouncementResponse> getList(String sortBy, String orderBy, Integer limit, Integer offset) {
		StringBuilder sql = new StringBuilder();
		sql.append(
				" SELECT CONCAT(u.fname, ' ', u.lname) AS userName, announcementid, icon, titletext, bodytext, author, dispauthor, startdate, expiredate, dispprojects ");
		sql.append(" FROM core.announcements a LEFT JOIN core.users u ON CAST(a.author AS smallint) = u.userid ");

		if (sortBy != null && orderBy != null) {
			if (sortBy.equals("startDate")) {
				sql.append(" ORDER BY startdate " + orderBy + " ");
			} else if (sortBy.equals("expiration")) {
				sql.append(" ORDER BY expiredate " + orderBy + "  ");

			} else if (sortBy.equals("title")) {
				sql.append(" ORDER BY titletext " + orderBy + " ");

			} else if (sortBy.equals("author")) {
				sql.append(" ORDER BY author " + orderBy + " ");

			}
		} else {
			sql.append(" ORDER BY startdate desc ");
		}
		if (limit != null) {
			sql.append(" LIMIT " + limit + " OFFSET  " + offset + "");
		}
		List<AnnouncementResponse> list = this.jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
			AnnouncementResponse announcement = new AnnouncementResponse();
			announcement.setAnnouncementId(rs.getLong("announcementid"));
			announcement.setTitle(rs.getString("titletext"));
			announcement.setIcon(rs.getString("icon"));
			announcement.setBodyText(rs.getString("bodytext"));
			announcement.setIsAuthor(rs.getBoolean("dispauthor"));
			announcement.setStartDate(rs.getDate("startdate"));
			announcement.setExpireDate(rs.getDate("expiredate"));
			String projectIds = rs.getString("dispprojects");
			if (projectIds != null && !projectIds.isEmpty()) {
				List<Long> projectIdList = Arrays.stream(projectIds.split("\\|")).map(Long::valueOf)
						.collect(Collectors.toList());
				List<String> projectNames = getProjectNames(projectIdList);
				announcement.setProjectNames(projectNames);
			}
			announcement.setAuthorName(rs.getString("userName"));
			return announcement;
		});

		sql = new StringBuilder();
		sql.append(" SELECT COUNT(*) AS count");
		sql.append(" FROM core.announcements ");
		Long count = jdbcTemplate.queryForObject(sql.toString(), Long.class);

		PageResponse<AnnouncementResponse> response = new PageResponse<AnnouncementResponse>();
		response.setCount(count);
		response.setData(list);
		return response;
	}

	public List<String> getProjectNames(List<Long> projectIdList) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT projectname FROM core.projects WHERE active = 1 AND projectType <> 4 ");
		if (projectIdList != null && !projectIdList.isEmpty()) {
			sql.append("AND projectid IN (");
			sql.append(projectIdList.stream().map(String::valueOf).collect(Collectors.joining(", ")));
			sql.append(") ");
		}
		sql.append("ORDER BY projectid");
		List<String> projectNames = jdbcTemplate.queryForList(sql.toString(), String.class);
		return projectNames;
	}
}
