package com.proep.api.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.proep.api.models.business.SessionUserEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.proep.api.models.business.CommunicationsHubEntry;
import com.proep.api.models.business.GeneralResponse;
import com.proep.api.models.dataaccess.CommunicationsHub;
import com.proep.api.models.dataaccess.CommunicationsHubAuditTable;
import com.proep.api.models.dataaccess.repos.CommunicationsHubAuditTableRepository;
import com.proep.api.models.dataaccess.repos.CommunicationsHubRepository;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/communicationsHub")
public class CommunicationsHubController {
	@Autowired
	private CommunicationsHubRepository communicationsHubRepository;
	@Autowired
	private CommunicationsHubAuditTableRepository communicationsHubAuditTableRepository;

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
			List<CommunicationsHubEntry> communicationsHubEntries = new ArrayList<CommunicationsHubEntry>();
			List<CommunicationsHub> communicationsHubFieldValues = communicationsHubRepository
					.findAllByOrderByEntryId();
			List<Integer> uniqueEntryIds = communicationsHubFieldValues.stream().map(CommunicationsHub::getEntryId)
					.distinct().collect(Collectors.toList());
			for (int uniqueEntryId : uniqueEntryIds) {
				List<CommunicationsHub> uniqueEntryFieldValues = communicationsHubFieldValues.stream()
						.filter(chfv -> chfv.getEntryId() == uniqueEntryId) // Filter by unique EntryId
						.collect(Collectors.toList()); // Collect filtered values into a List

				communicationsHubEntries.add(new CommunicationsHubEntry(uniqueEntryFieldValues));
			}
			response.Status = "Success";
			response.Message = "Successfully retrieved communications hub entries";
			response.Subject = communicationsHubEntries;
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	@GetMapping("/{projectId}")
	public GeneralResponse getRequestsByNetId(@PathVariable int projectId) {
		GeneralResponse response = new GeneralResponse();
		try {
			List<CommunicationsHubEntry> communicationsHubEntries = new ArrayList<CommunicationsHubEntry>();
			List<CommunicationsHub> communicationsHubFieldValues = communicationsHubRepository
					.findAllByOrderByEntryId();
			List<Integer> uniqueEntryIds = communicationsHubFieldValues.stream().map(CommunicationsHub::getEntryId)
					.distinct().collect(Collectors.toList());
			for (int uniqueEntryId : uniqueEntryIds) {
				List<CommunicationsHub> uniqueEntryFieldValues = communicationsHubFieldValues.stream()
						.filter(chfv -> chfv.getEntryId() == uniqueEntryId) // Filter by unique EntryId
						.collect(Collectors.toList()); // Collect filtered values into a List

				communicationsHubEntries.add(new CommunicationsHubEntry(uniqueEntryFieldValues));
			}
			response.Status = "Success";
			response.Message = "Successfully retrieved communications hub entries for project ID" + projectId;
			response.Subject = communicationsHubEntries;
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	@PostMapping
	public GeneralResponse saveCommunicationsHubEntries(HttpServletRequest httpServletRequest,
			@RequestBody List<CommunicationsHubEntry> communicationsHubEntries) {
		GeneralResponse response = new GeneralResponse();
		String netId = "Unknown";
		// Retrieve NetId from session if available
		Object netIdObj = httpServletRequest.getSession().getAttribute("NetId");
		if (netIdObj != null) {
			netId = (String) netIdObj;
		}
		int maxEntryId = communicationsHubRepository.findMaxEntryId()+1;
		List<CommunicationsHub> savedCommunicationsHubFieldValues = new ArrayList<CommunicationsHub>();
		List<String> errorMessages = new ArrayList<String>();

		try {
			for (CommunicationsHubEntry entry : communicationsHubEntries) {
				for (CommunicationsHub fieldValue : entry.getFieldValues()) {
					try {
						if(fieldValue.getCommunicationsHubId() < 1) {
							fieldValue.setEntryId(maxEntryId);
						}
						CommunicationsHub ch = communicationsHubRepository.save(fieldValue);
						savedCommunicationsHubFieldValues.add(ch);
					} catch (Exception ex) {
						errorMessages.add(ex.getMessage());
					}
				}
			}
			response.Status = "Success";
			response.Message = "Successfully saved Communications Hub entries";
			response.Subject = savedCommunicationsHubFieldValues;
			if (errorMessages.size() > 0) {
				response.Message = String.format(
						"%d Communications Hub entries saved, %d Communications Hub entries failed to save:\n%s",
						savedCommunicationsHubFieldValues.size(), errorMessages.size(),
						String.join("\n", errorMessages));
			}
			if (savedCommunicationsHubFieldValues.size() < 1) {
				throw new Exception("No Communications Hub entries were saved in the database");
			}
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	@DeleteMapping
	public GeneralResponse deleteCommunicationsHubEntries(HttpServletRequest httpServletRequest,
			@RequestBody List<CommunicationsHubEntry> communicationsHubEntries) {
		GeneralResponse response = new GeneralResponse();
		String netId = "Unknown";
		// Retrieve NetId from session if available
		Object netIdObj = httpServletRequest.getSession().getAttribute("NetId");
		if (netIdObj != null) {
			netId = (String) netIdObj;
		}
		List<CommunicationsHub> deletedCommunicationsHubFieldValues = new ArrayList<CommunicationsHub>();
		List<String> errorMessages = new ArrayList<String>();

		try {
			for (CommunicationsHubEntry entry : communicationsHubEntries) {
				for (CommunicationsHub fieldValue : entry.getFieldValues()) {
					try {
						communicationsHubRepository.delete(fieldValue);
						deletedCommunicationsHubFieldValues.add(fieldValue);
					} catch (Exception ex) {
						errorMessages.add(ex.getMessage());
					}
				}
				Thread.sleep(1000);
				boolean waited = false;
				for (CommunicationsHub fieldValue : entry.getFieldValues()) {
					CommunicationsHubAuditTable chAudit = communicationsHubAuditTableRepository
							.findFirstByCommunicationsHubIdAndAuditAction(fieldValue.getCommunicationsHubId())
							.orElse(null);
					if (chAudit == null && !waited) {
						Thread.sleep(1000);
						waited = true;
						chAudit = communicationsHubAuditTableRepository
								.findFirstByCommunicationsHubIdAndAuditAction(fieldValue.getCommunicationsHubId())
								.orElse(null);
					}
					if (chAudit != null) {
						chAudit.setAuditUser(fieldValue.getModBy());
						communicationsHubAuditTableRepository.save(chAudit);
					}
				}

			}
			response.Status = "Success";
			response.Message = "Successfully deleted Communications Hub entries";
			response.Subject = deletedCommunicationsHubFieldValues;

			if (errorMessages.size() > 0) {
				response.Message = String.format(
						"%d Communications Hub entries deleted, %d Communications Hub entries failed to delete:\n%s",
						deletedCommunicationsHubFieldValues.size(), errorMessages.size(),
						String.join("\n", errorMessages));
			}
			if (deletedCommunicationsHubFieldValues.size() < 1) {
				throw new Exception("No Communications Hub entries were deleted from the database");
			}
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

}
