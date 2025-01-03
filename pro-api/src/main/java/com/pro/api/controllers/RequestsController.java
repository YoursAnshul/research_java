package com.pro.api.controllers;

import com.pro.api.models.business.SessionUserEmail;
import com.pro.api.models.dataaccess.Request;
import com.pro.api.models.dataaccess.repos.RequestRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class RequestsController {
	@Autowired
	private RequestRepository requestRepository;

	@Autowired
	private SessionUserEmail UserEmail;

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

}
