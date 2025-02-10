package com.pro.api.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.pro.api.service.AuditService;
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
import org.springframework.transaction.annotation.Transactional;

@Service
public class ManageAnnouncementsImpl implements ManageAnnouncements {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private AuditService auditService;

	@Override
	public List<AuthorResponse> getAllAuthors() {
		StringBuilder sql = new StringBuilder();
		sql.append(" SELECT userid, CONCAT(fname, ' ', lname) AS userName  ");
		sql.append(" FROM core.users  ");
		sql.append(" ORDER BY userName ASC ");
		List<AuthorResponse> list = this.jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
			AuthorResponse obj = new AuthorResponse();
			obj.setUserId(rs.getLong("userid"));
			obj.setUserName(rs.getString("userName"));
			return obj;
		});
		return list;
	}

	@Override
	public AuthorResponse getLoginUser(String email) {
		StringBuilder sql = new StringBuilder();
		sql.append(" SELECT userid, CONCAT(fname, ' ', lname) AS userName, dispauthor  ");
		sql.append(
				" FROM core.users u LEFT JOIN core.announcements a ON CAST(a.author AS smallint) = u.userid  WHERE emailaddr = '"
						+ email + "' ");
		sql.append(" ORDER BY userName ASC ");
		List<AuthorResponse> list = this.jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
			AuthorResponse obj = new AuthorResponse();
			obj.setUserId(rs.getLong("userid"));
			obj.setUserName(rs.getString("userName"));
			obj.setIsAuthor(rs.getBoolean("dispauthor"));
			return obj;
		});
		return list != null && !list.isEmpty() ? list.get(0) : new AuthorResponse();
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
		String sql = "";
		int rowsAffected = 0;
		AuthorResponse loginUserObj = getLoginUser(request.getEmail());
		String userName = "";
		if (loginUserObj != null) {
			userName = loginUserObj.getUserName();
		}
		if (request.getAnnouncementId() != null && request.getAnnouncementId() > 0) {
			sql = "UPDATE core.announcements SET titletext = ?, bodytext = ?, author = ?, dispauthor = ?, "
					+ "startdate = ?, expiredate = ?, dispprojects = ?, icon = ?, moddt=NOW(), modBy=? WHERE announcementid = ?";
			rowsAffected = this.jdbcTemplate.update(sql, request.getTitle(), request.getBodyText(),
					request.getAuthorId(), request.getIsAuthor(), request.getStartDate(), request.getExpireDate(),
					projectIds, request.getIcon(), userName, request.getAnnouncementId());

		} else {
			sql = "INSERT INTO core.announcements (titletext, bodytext, author, dispauthor, "
					+ "startdate, expiredate, dispprojects, icon, entrydt, entryby) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			rowsAffected = this.jdbcTemplate.update(sql, request.getTitle(), request.getBodyText(),
					request.getAuthorId(), request.getIsAuthor(), request.getStartDate(), request.getExpireDate(),
					projectIds, request.getIcon(), new Date(), userName);

		}

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
	public PageResponse<AnnouncementResponse> getList(String sortBy, String orderBy, Integer limit, Integer offset,
			String keyword, String authorName) {
		StringBuilder sql = new StringBuilder();
		sql.append(" SELECT a.announcementid, CONCAT(u.fname, ' ', u.lname) AS userName,  a.icon, ");
		sql.append(" a.titletext,  a.bodytext,  a.author, a.dispauthor, a.startdate, a.expiredate,  p.dispprojects ");
		sql.append(" FROM core.announcements a ");
		sql.append(" LEFT JOIN core.users u ON CAST(a.author AS smallint) = u.userid ");
		sql.append(" LEFT JOIN LATERAL (SELECT  ");
		sql.append(" STRING_AGG(p.projectid::text, ',' ORDER BY p.projectid) AS dispprojects ");
		sql.append("  FROM  core.projects p ");
		sql.append("  WHERE p.projectid = ANY(string_to_array(a.dispprojects, '|')::int[]) ");
		sql.append(" ) AS p ON TRUE  WHERE 1=1 ");
		if (keyword != null && !keyword.isEmpty()) {
			keyword = keyword.toLowerCase();
			sql.append(" AND  LOWER(a.titletext) LIKE  '%" + keyword + "%' ");
		}
		if (authorName != null && !authorName.isEmpty() && !authorName.equals("''")) {
			sql.append(" AND CONCAT(u.fname, ' ', u.lname) IN (" + authorName + ") ");
		}
		System.out.println(sql.toString());
		if (sortBy != null && orderBy != null) {
			if (sortBy.equals("startdate")) {
				sql.append(" ORDER BY startdate " + orderBy + " ");
			} else if (sortBy.equals("expiration")) {
				sql.append(" ORDER BY expiredate " + orderBy + "  ");

			} else if (sortBy.equals("title")) {
				sql.append(" ORDER BY titletext " + orderBy + " ");

			} else if (sortBy.equals("author")) {
				sql.append(" ORDER BY userName " + orderBy + " ");

			}
		} else {
			sql.append(" ORDER BY a.startdate  desc ");
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
			List<Long> projectIdList = new ArrayList<>();
			if (projectIds != null && !projectIds.trim().isEmpty()) {
				String[] projectIdArr = projectIds.split(",");
				for (String id : projectIdArr) {
					try {
						id = id.trim();
						long projectId = Long.parseLong(id);
						projectIdList.add(projectId);
					} catch (NumberFormatException e) {
						throw new IllegalArgumentException("Invalid project ID format: " + id, e);
					}
				}
				announcement.setProjectIds(projectIdList);
			}
			announcement.setAuthorName(rs.getString("userName"));
			if (projectIdList != null && !projectIdList.isEmpty()) {
				announcement.setProjectObject(getProjectObject(projectIdList));
			}
			return announcement;
		});

		sql = new StringBuilder();
		sql.append(" SELECT COUNT(*) AS count");
		sql.append(" FROM core.announcements a ");
		sql.append(" LEFT JOIN core.users u ON CAST(a.author AS smallint) = u.userid WHERE 1=1 ");
		if (keyword != null && !keyword.isEmpty()) {
			keyword = keyword.toLowerCase();
			sql.append(" AND  LOWER(a.titletext) LIKE  '%" + keyword + "%' ");
		}
		if (authorName != null && !authorName.isEmpty()) {
			sql.append(" AND CONCAT(u.fname, ' ', u.lname) IN (" + authorName + ") ");
		}
		Long count = jdbcTemplate.queryForObject(sql.toString(), Long.class);

		PageResponse<AnnouncementResponse> response = new PageResponse<AnnouncementResponse>();
		response.setCount(count);
		response.setData(list);
		return response;
	}

	@Override
	public List<ProjectResponse> getProjectObject(List<Long> projectIdList) {
		StringBuilder sql = new StringBuilder();
		sql.append(
				"SELECT projectid, projectcolor,projectname FROM core.projects WHERE active = 1 AND projectType <> 4 ");
		sql.append("AND projectid IN (");
		sql.append(projectIdList.stream().map(String::valueOf).collect(Collectors.joining(", ")));
		sql.append(") ");
		sql.append("ORDER BY projectname ASC ");
		List<ProjectResponse> projectResponses = jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
			ProjectResponse projectResponse = new ProjectResponse();
			projectResponse.setProjectId(rs.getLong("projectid"));
			projectResponse.setProjectName(rs.getString("projectname"));
			projectResponse.setProjectColor(rs.getString("projectcolor"));
			return projectResponse;
		});

		return projectResponses;
	}

	@Override
	@Transactional
	public GeneralResponse delete(Integer id,String userName) {
		String sql = "DELETE FROM core.announcements WHERE announcementid = '" + id + "'";

		this.auditService.updateNetId(userName);
		this.jdbcTemplate.execute(sql);
		GeneralResponse respone = new GeneralResponse();
		respone.Message = "Delete successfully!!";
		return respone;
	}

	@Override
	public List<AuthorResponse> getAuthors() {
		StringBuilder sql = new StringBuilder();
		sql.append(" SELECT DISTINCT u.userid, CONCAT(u.fname, ' ', u.lname) AS userName ");
		sql.append(" FROM core.announcements a ");
		sql.append(" INNER JOIN core.users u ON CAST(a.author AS smallint) = u.userid ");
		List<AuthorResponse> list = jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
			AuthorResponse res = new AuthorResponse();
			res.setUserName(rs.getString("userName"));
			res.setUserId(rs.getLong("userid"));
			return res;
		});
		return list;
	}

	@Override
	public GeneralResponse getAnnouncementList() {
		StringBuilder sql = new StringBuilder();
		sql.append(" SELECT a.announcementid, CONCAT(u.fname, ' ', u.lname) AS userName, a.icon, ");
		sql.append(" a.titletext, a.bodytext, a.author, a.dispauthor, a.startdate, a.expiredate, p.dispprojects ");
		sql.append(" FROM core.announcements a ");
		sql.append(" LEFT JOIN core.users u ON CAST(a.author AS smallint) = u.userid ");
		sql.append(" LEFT JOIN LATERAL (SELECT ");
		sql.append(" STRING_AGG(p.projectid::text, ',' ORDER BY p.projectid) AS dispprojects ");
		sql.append(" FROM core.projects p ");
		sql.append(" WHERE p.projectid = ANY(string_to_array(a.dispprojects, '|')::int[]) ");
		sql.append(" ) AS p ON TRUE ");
		sql.append(
				" WHERE a.startdate <= CURRENT_DATE AND (a.expiredate >= CURRENT_DATE or a.expiredate is null)  ORDER BY a.startdate DESC ");

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
			List<Long> projectIdList = new ArrayList<>();
			if (projectIds != null && !projectIds.trim().isEmpty()) {
				String[] projectIdArr = projectIds.split(",");
				for (String id : projectIdArr) {
					try {
						id = id.trim();
						long projectId = Long.parseLong(id);
						projectIdList.add(projectId);
					} catch (NumberFormatException e) {
						throw new IllegalArgumentException("Invalid project ID format: " + id, e);
					}
				}
				announcement.setProjectIds(projectIdList);
			}

			announcement.setAuthorName(rs.getString("userName"));
			if (projectIdList != null && !projectIdList.isEmpty()) {
				announcement.setProjectObject(getProjectObject(projectIdList));
			}
			return announcement;
		});

		GeneralResponse response = new GeneralResponse();
		response.Subject = list;
		return response;
	}

}
