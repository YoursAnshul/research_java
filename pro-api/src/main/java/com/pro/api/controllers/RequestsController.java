package com.pro.api.controllers;

import com.pro.api.models.business.SessionUserEmail;
import com.pro.api.models.dataaccess.Request;
import com.pro.api.models.dataaccess.repos.RequestRepository;
import com.pro.api.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class RequestsController {
	@Autowired
	private RequestRepository requestRepository;

	@Autowired
	private SessionUserEmail UserEmail;

	@Autowired
	private AuditService auditService;

	@ModelAttribute("UserEmail")
	public SessionUserEmail getUserEmail() {
		return UserEmail;
	}

	@GetMapping
	public GeneralResponse index() {
		GeneralResponse response = new GeneralResponse();
		try {

			List<Request> requests = requestRepository.findAllByOrderByRequestDateDesc();

			response.Status = "Success";
			response.Message = "Successfully retrieved requests";
			response.Subject = requests;
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	@GetMapping("/{netId}")
	public GeneralResponse getRequestsByNetId(@PathVariable String netId) {
		GeneralResponse response = new GeneralResponse();
		try {
			List<Request> requests = requestRepository.findAllByInterviewerEmpIdOrderByRequestDateDesc(netId);
			response.Status = "Success";
			response.Message = "Successfully retrieved requests for " + netId;
			response.Subject = requests;
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}
		@PostMapping("/update")
		public GeneralResponse updateRequests(HttpServletRequest httpServletRequest, @RequestBody List<Request> requests) {
			GeneralResponse response = new GeneralResponse();
			String netId = "Unknown";
			// Retrieve NetId from session if available
			Object netIdObj = httpServletRequest.getSession().getAttribute("NetId");
			if (netIdObj != null) {
				netId = (String) netIdObj;
			}
			List<Request> savedRequests = new ArrayList<Request>();
			List<String> errorMessages = new ArrayList<String>();
			try {
				for (Request request : requests) {
					try {
						try {
							auditService.updateNetId(request.getModBy());
							Request existRequest = requestRepository.findByScheduleId(request.getScheduleId());
							if (existRequest != null) {

								if (request.getRequestCodeId() != null) {
									existRequest.setRequestCodeId(request.getRequestCodeId());
								}

								if (request.getInterviewerEmpId() != null) {
									existRequest.setInterviewerEmpId(request.getInterviewerEmpId());
								}

								if (request.getResourceTeamMemberId() != null) {
									existRequest.setResourceTeamMemberId(request.getResourceTeamMemberId());
								}

								if (request.getRequestDate() != null) {
									existRequest.setRequestDate(request.getRequestDate());
								}

								if (request.getRequestDetails() != null) {
									existRequest.setRequestDetails(request.getRequestDetails());
								}

								if (request.getDecisionId() != null) {
									existRequest.setDecisionId(request.getDecisionId());
								}

								if (request.getNotes() != null) {
									existRequest.setNotes(request.getNotes());
								}

								if (request.getEntryBy() != null) {
									existRequest.setEntryBy(request.getEntryBy());
								}

								if (request.getEntryDt() != null) {
									existRequest.setEntryDt(request.getEntryDt());
								}

								if (request.getModBy() != null) {
									existRequest.setModBy(request.getModBy());
								}

								existRequest.setModDt(LocalDateTime.now());
								Request rq = requestRepository.save(existRequest);
								savedRequests.add(rq);
							} else {
								Request rq = requestRepository.save(request);
								savedRequests.add(rq);
							}

						} catch (Exception ex) {
							throw new Exception("Unspecified error saving to database");
						}
					} catch (Exception ex) {
						errorMessages.add(ex.getMessage());
					}
				}
				response.Status = "Success";
				response.Message = "Successfully update request(s)";
				response.Subject = savedRequests;
				if (errorMessages.size() > 0) {
					response.Message = String.format("%d requests saved, %d requests failed to save:\n%s",
							savedRequests.size(), errorMessages.size(), String.join("\n", errorMessages));
				}
				if (savedRequests.size() < 1) {
					throw new Exception("No request(s) were saved in the database");
				}
			} catch (Exception ex) {
				response.Status = "Failure";
				response.Message = ex.getMessage();
			}

			return response;
		}

	@PostMapping
	public GeneralResponse saveRequests(HttpServletRequest httpServletRequest, @RequestBody List<Request> requests) {
		GeneralResponse response = new GeneralResponse();
		String netId = "Unknown";
		// Retrieve NetId from session if available
		Object netIdObj = httpServletRequest.getSession().getAttribute("NetId");
		if (netIdObj != null) {
			netId = (String) netIdObj;
		}
		List<Request> savedRequests = new ArrayList<Request>();
		List<String> errorMessages = new ArrayList<String>();
		try {
			for (Request request : requests) {
				try {
					try {
						auditService.updateNetId(request.getModBy());
						Request rq = requestRepository.save(request);
						savedRequests.add(rq);
					} catch (Exception ex) {
						throw new Exception("Unspecified error saving to database");
					}
				} catch (Exception ex) {
					errorMessages.add(ex.getMessage());
				}
			}
			response.Status = "Success";
			response.Message = "Successfully saved request(s)";
			response.Subject = savedRequests;
			if (errorMessages.size() > 0) {
				response.Message = String.format("%d requests saved, %d requests failed to save:\n%s",
						savedRequests.size(), errorMessages.size(), String.join("\n", errorMessages));
			}
			if (savedRequests.size() < 1) {
				throw new Exception("No request(s) were saved in the database");
			}
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	@DeleteMapping
	public GeneralResponse deleteSchedules(HttpServletRequest httpServletRequest, @RequestBody List<Request> requests) {
		GeneralResponse response = new GeneralResponse();
		String netId = "Unknown";
		// Retrieve NetId from session if available
		Object netIdObj = httpServletRequest.getSession().getAttribute("NetId");
		if (netIdObj != null) {
			netId = (String) netIdObj;
		}
		List<Request> deletedRequests = new ArrayList<Request>();
		List<String> errorMessages = new ArrayList<String>();
		try {
			for (Request request : requests) {
				try {
					auditService.updateNetId(request.getModBy());
					requestRepository.delete(request);
					deletedRequests.add(request);
				} catch (Exception ex) {
					errorMessages.add(ex.getMessage());
				}
			}
			response.Status = "Success";
			response.Message = "Successfully deleted request(s)";
			response.Subject = deletedRequests;
			if (errorMessages.size() > 0) {
				response.Message = String.format("%d requests deleted, %d requests failed to save:\n%s",
						deletedRequests.size(), errorMessages.size(), String.join("\n", errorMessages));
			}
			if (deletedRequests.size() < 1) {
				throw new Exception("No request(s) were deleted from the database");
			}
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	

	@DeleteMapping("/{id}")
	public GeneralResponse deleteRequest(@PathVariable Long id) {
		GeneralResponse response = new GeneralResponse();
		try {
			Request existRequest = requestRepository.findByScheduleId(id);
			requestRepository.delete(existRequest);
			response.Status = "Success";
			response.Message = "Successfully delete request";
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}
		return response;
	}

	

}
