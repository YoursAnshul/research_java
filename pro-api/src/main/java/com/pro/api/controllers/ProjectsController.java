package com.pro.api.controllers;

import com.pro.api.models.business.ProjectMin;
import com.pro.api.models.business.ProjectTotalsReport;
import com.pro.api.models.business.SessionUserEmail;
import com.pro.api.models.dataaccess.*;
import com.pro.api.models.dataaccess.repos.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/projects")
public class ProjectsController {
	@Autowired
	private ProjectRepository projectRepository;
	@Autowired
	private ForecastHourRepository forecastHourRepository;
	@Autowired
	private DropDownValueRepository dropDownValueRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ViProjectTotalsSummedRepository viProjectTotalsSummedRepository;
	@Autowired
	private ViProjectTotalRepository viProjectTotalRepository;

	@Autowired
	private SessionUserEmail UserEmail;

	@ModelAttribute("UserEmail")
	public SessionUserEmail getUserEmail() {
		return UserEmail;
	}

	@GetMapping
	public GeneralResponse getProjects() {
		GeneralResponse response = new GeneralResponse();
		try {

			response.Status = "Success";
			response.Message = "Successfully retrieved projects";
			response.Subject = projectRepository.findAll();
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	@GetMapping("/min")
	public GeneralResponse getProjectsMin() {
		GeneralResponse response = new GeneralResponse();
		List<ProjectMin> minProjects = new ArrayList<ProjectMin>();
		try {
			try {
				List<DropDownValue> projectTypes = dropDownValueRepository.findByTableNameAndColumnName("Projects",
						"ProjectType");
				List<DropDownValue> displayedInOptions = dropDownValueRepository
						.findByTableNameAndColumnName("Projects", "ProjectDisplayID");
				List<Project> projects = projectRepository.findAllByOrderByProjectName();
				for (Project project : projects) {
					Optional<DropDownValue> projectTypeDD = dropDownValueRepository.findByFormFieldId(6).stream().filter(s->s.getCodeValues() == project.getProjectType()).findFirst();
					DropDownValue projectType  = projectTypeDD.orElseGet(DropDownValue::new);
					minProjects.add(new ProjectMin(project.getProjectId(), project.getProjectName(),
							project.getProjectAbbr(), project.getProjectDisplayId(), "", project.getActive(),
							project.getProjectStatus(), project.getProjectColor(), projectType.getDropDownItem()));

				}
			} catch (Exception ex) {
				minProjects.add(new ProjectMin() {
					{
						setProjectName("A database or network issue occurred and no projects could be retrieved.");
					}
				});
			}
			response.Status = "Success";
			response.Message = "Successfully retrieved projects";
			response.Subject = minProjects;
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	@GetMapping("/active")
	public GeneralResponse getActiveProjects() {
		GeneralResponse response = new GeneralResponse();
		try {

			response.Status = "Success";
			response.Message = "Successfully retrieved active projects";
			response.Subject = projectRepository.findAllByActiveOrderByProjectName(1);
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	@GetMapping("/activeIds")
	public GeneralResponse getActiveProjectsIds() {
		GeneralResponse response = new GeneralResponse();
		try {

			response.Status = "Success";
			response.Message = "Successfully retrieved active project ids";
			response.Subject = projectRepository.findSortedActiveProjectIds();
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	@GetMapping("/{id}")
	public GeneralResponse getProject(@PathVariable Integer id) {
		GeneralResponse response = new GeneralResponse();
		try {
			response.Status = "Success";
			response.Message = "Successfully retrieved project having id " + id;
			response.Subject = projectRepository.findById(id);
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	@GetMapping("/projectTotals")
	public GeneralResponse getProjectTotal(@RequestParam("weekStart") LocalDate weekStartParam,
			@RequestParam("weekEnd") LocalDate weekEndParam) {
		GeneralResponse response = new GeneralResponse();
		try {
			OffsetDateTime weekStart = weekStartParam.atStartOfDay().atOffset(ZoneOffset.UTC);
			OffsetDateTime weekEnd = weekEndParam.atStartOfDay().atOffset(ZoneOffset.UTC);
			ProjectTotalsReport projectTotalsReport = new ProjectTotalsReport();
			List<ViProjectTotal> projectTotals = viProjectTotalRepository.findProjectTotalsByWeekRange(weekStart, weekEnd);
			weekEnd = weekEnd.plusDays(1);
			List<ViProjectTotalsSummed> projectTotalsSummed = viProjectTotalsSummedRepository
					.findProjectTotalsSummedByWeekRange(weekStart.toLocalDate().atStartOfDay().atOffset(weekStart.getOffset()),
					weekEnd.toLocalDate().atStartOfDay().atOffset(weekEnd.getOffset()));
			projectTotalsReport.setProjectTotals(projectTotals);
			projectTotalsReport.setProjectTotalsSummed(projectTotalsSummed);
			response.Status = "Success";
            response.Message = "Successfully retrieved project totals report info";
            response.Subject = projectTotalsReport;
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	@PutMapping("/{id}")
	public GeneralResponse putProject(@PathVariable Integer id, @RequestBody Project project) {
		GeneralResponse response = new GeneralResponse();
		try {
			if (id != project.getProjectId())
				throw new Exception("Project ID not provided");
			projectRepository.save(project);
			response.Status = "Success";
			response.Message = "Successfully saved project having id " + id;
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	@PostMapping
	public GeneralResponse saveProject(@RequestBody Project project) {
		GeneralResponse response = new GeneralResponse();
		try {
			try {
				project = projectRepository.save(project);
			} catch (Exception ex) {
				throw new Exception("Unspecified error saving to database");
			}
			response.Status = "Success";
			response.Message = "Successfully saved project";
			response.Subject = project;
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	@PostMapping("/trainedOn")
	public GeneralResponse saveProjectTrainedOn(@RequestBody ProjectMin project) {
		GeneralResponse response = new GeneralResponse();
		try {
			for (String netId : project.getTrainedOnUsers()) {
				User userToAdd = userRepository.findFirstByDempoidIgnoreCase(netId);
				if (userToAdd != null) {
					List<String> userTrainedOn = new ArrayList<String>();
					if (userToAdd.getTrainedon() != null) {
						userTrainedOn = new ArrayList(Arrays.asList(userToAdd.getTrainedon().split("\\|")));
					}
					// if the user trained on field does not contain the project, add it
					if (!userTrainedOn.contains(String.valueOf(project.getProjectID()))) {
						userTrainedOn.add(String.valueOf(project.getProjectID()));
						String trainedOn = String.join("|", userTrainedOn);
						userToAdd.setTrainedon(trainedOn);
						try {
							userRepository.save(userToAdd);
						} catch (Exception ex) {
							throw new Exception("Unspecified error saving to database");
						}
					}
				}
			}

			// not trained on (remove)
			for (String netId : project.getNotTrainedOnUsers()) {
				User userToRemove = userRepository.findFirstByDempoidIgnoreCase(netId);
				if (userToRemove != null) {
					List<String> userTrainedOn = new ArrayList<String>();
					if (userToRemove.getTrainedon() != null) {
						userTrainedOn = Arrays.asList(userToRemove.getTrainedon().split("\\|"));
					}
					// if the user trained on field does not contain the project, add it
					if (!userTrainedOn.contains(String.valueOf(project.getProjectID()))) {
						userTrainedOn.remove(String.valueOf(project.getProjectID()));
						String trainedOn = String.join("|", userTrainedOn);
						userToRemove.setTrainedon(trainedOn);
						try {
							userRepository.save(userToRemove);
						} catch (Exception ex) {
							throw new Exception("Unspecified error saving to database");
						}
					}
				}
			}

			response.Status = "Success";
			response.Message = "Successfully saved project trained on users";
			response.Subject = project;
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	@DeleteMapping("/{id}")
	public GeneralResponse deleteProject(@PathVariable Integer id) {
		GeneralResponse response = new GeneralResponse();
		try {
			projectRepository.deleteById(id);
			response.Status = "Success";
			response.Message = "Successfully deleted project having id " + id;
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	@GetMapping("/forecasting/{projectId}")
	public GeneralResponse getProjectForecasting(@PathVariable Integer projectId) {
		GeneralResponse response = new GeneralResponse();
		try {
			response.Status = "Success";
			response.Message = "Successfully retrieved forecasting hours";
			response.Subject = forecastHourRepository.findFirstByProjectId(projectId);
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	@GetMapping("/forecasting")
	public GeneralResponse getAllProjectForecasting() {
		GeneralResponse response = new GeneralResponse();
		try {
			response.Status = "Success";
			response.Message = "Successfully retrieved forecasting hours";
			response.Subject = forecastHourRepository.findAll();
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	@PostMapping("/forecasting")
	public GeneralResponse saveForecasting(HttpServletRequest request, @RequestBody List<ForecastHour> forecasting) {
		GeneralResponse response = new GeneralResponse();
		String netId = "Unknown";
		// Retrieve NetId from session if available
		Object netIdObj = request.getSession().getAttribute("NetId");
		if (netIdObj != null) {
			netId = (String) netIdObj;
		}

		try {
			for (ForecastHour forecastHours : forecasting) {
				try {
					forecastHourRepository.save(forecastHours);
				} catch (Exception ex) {
					throw new Exception("Unspecified error saving to database");
				}
				response.Status = "Success";
				response.Message = "Successfully saved forecasting";
				response.Subject = forecastHours;
			}
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	@GetMapping("/forecasting/reconcile")
	public GeneralResponse reconcileForecasting() {
		GeneralResponse response = new GeneralResponse();
		try {
			List<ForecastHour> forecastHours = forecastHourRepository.findAll();
			LocalDate currentMonthStart = LocalDate.now().withDayOfMonth(1);
			boolean conditionMet = forecastHours.stream().anyMatch(f -> f.getModDt().isBefore(currentMonthStart))
					|| forecastHours.isEmpty();
			if (conditionMet) {
				forecastingReconciliation(true);
				// reconcile inactive projects as well, but do them separately
				forecastingReconciliation(false);

				response.Status = "Success";
				response.Message = "Successfully reconciled Forecasting";
			} else {
				response.Status = "Success";
				response.Message = "Forecasting already reconciled";
			}
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	private void forecastingReconciliation(boolean active) throws Exception {
		LocalDate currentMonthStart = LocalDate.now().withDayOfMonth(1);
		List<Project> projects = projectRepository.findByActive(active ? 1 : 0);
		List<ForecastHour> forecastHoursList = new ArrayList<ForecastHour>();

		for (Project project : projects) {
			ForecastHour forecastHours = forecastHourRepository.findFirstByProjectId(project.getProjectId());
			// if we don't find an entry, we have an issue and need to create initial
			// forecast entry for the project
			if (forecastHours == null) {
				forecastHours = initialForecastHours(project.getProjectId());
			}
			// if modified
			if (forecastHours.getForecastHoursId() > 0) {
				// Check if dates are correct or not
				int dateDifference = ((currentMonthStart.getYear() - forecastHours.getMonth2().getYear()) * 12)
						+ (currentMonthStart.getMonthValue() - forecastHours.getMonth2().getMonthValue());

				List<Integer> allProjectForecastHours = Arrays.asList(forecastHours.getForecastHours1(),
						forecastHours.getForecastHours2(), forecastHours.getForecastHours3(),
						forecastHours.getForecastHours4(), forecastHours.getForecastHours5(),
						forecastHours.getForecastHours6(), forecastHours.getForecastHours7(),
						forecastHours.getForecastHours8(), forecastHours.getForecastHours9(),
						forecastHours.getForecastHours10(), forecastHours.getForecastHours11(),
						forecastHours.getForecastHours12(), forecastHours.getForecastHours13(),
						forecastHours.getForecastHours14());

				List<Integer> allProjectFreq = Arrays.asList(forecastHours.getFreq1(), forecastHours.getFreq2(),
						forecastHours.getFreq3(), forecastHours.getFreq4(), forecastHours.getFreq5(),
						forecastHours.getFreq6(), forecastHours.getFreq7(), forecastHours.getFreq8(),
						forecastHours.getFreq9(), forecastHours.getFreq10(), forecastHours.getFreq11(),
						forecastHours.getFreq12(), forecastHours.getFreq13(), forecastHours.getFreq14());

				List<Boolean> allWeekChk = Arrays.asList(forecastHours.getWeekchk1(), forecastHours.getWeekchk2(),
						forecastHours.getWeekchk3(), forecastHours.getWeekchk4(), forecastHours.getWeekchk5(),
						forecastHours.getWeekchk6(), forecastHours.getWeekchk7(), forecastHours.getWeekchk8(),
						forecastHours.getWeekchk9(), forecastHours.getWeekchk10(), forecastHours.getWeekchk11(),
						forecastHours.getWeekchk12(), forecastHours.getWeekchk13(), forecastHours.getWeekchk14());

				List<Boolean> allWeekendChk = Arrays.asList(forecastHours.getWeekendChk1(),
						forecastHours.getWeekendChk2(), forecastHours.getWeekendChk3(), forecastHours.getWeekendChk4(),
						forecastHours.getWeekendChk5(), forecastHours.getWeekendChk6(), forecastHours.getWeekendChk7(),
						forecastHours.getWeekendChk8(), forecastHours.getWeekendChk9(), forecastHours.getWeekendChk10(),
						forecastHours.getWeekendChk11(), forecastHours.getWeekendChk12(),
						forecastHours.getWeekendChk13(), forecastHours.getWeekendChk14());

				List<String> allComments = Arrays.asList(forecastHours.getComment1(), forecastHours.getComment2(),
						forecastHours.getComment3(), forecastHours.getComment4(), forecastHours.getComment5(),
						forecastHours.getComment6(), forecastHours.getComment7(), forecastHours.getComment8(),
						forecastHours.getComment9(), forecastHours.getComment10(), forecastHours.getComment11(),
						forecastHours.getComment12(), forecastHours.getComment13(), forecastHours.getComment14());
				if (dateDifference > 0) {
					Integer lastForecastHours = allProjectForecastHours.get(13);
					Integer lastFreq = allProjectFreq.get(13);
					Boolean lastWeekChk = allWeekChk.get(13);
					Boolean lastWeekendChk = allWeekendChk.get(13);
					String lastComment = allComments.get(13);
					if (dateDifference <= 12) {
						allProjectForecastHours.subList(0, dateDifference).clear();
						allProjectFreq.subList(0, dateDifference).clear();
						allWeekChk.subList(0, dateDifference).clear();
						allWeekendChk.subList(0, dateDifference).clear();
						allComments.subList(0, dateDifference).clear();
						// add that difference to the end, using the last values
						for (int i = 0; i < dateDifference; i++) {
							allProjectForecastHours.add(lastForecastHours);
							allProjectFreq.add(lastFreq);
							allWeekChk.add(lastWeekChk);
							allWeekendChk.add(lastWeekendChk);
							allComments.add("");
						}
					}
					if (dateDifference > 12) {
						int numberOfElements = 14; // Number of elements in the lists

						// Initialize lists with the same value for each element
						for (int i = 0; i < numberOfElements; i++) {
							allProjectForecastHours.add(lastForecastHours);
							allProjectFreq.add(lastFreq);
							allWeekChk.add(lastWeekChk);
							allWeekendChk.add(lastWeekendChk);
							allComments.add("");
						}
					}
					setCurrentMonthForecastHours(forecastHours, allProjectForecastHours, allProjectFreq, allWeekChk,
							allWeekendChk, allComments);
					forecastHours.setModBy("SYSTEM");
					forecastHours.setModDt(LocalDate.now());
				}
			}
			forecastHoursList.add(forecastHours);
		}
		try {
			forecastHourRepository.saveAll(forecastHoursList);
		} catch (Exception ex) {
			throw new Exception("Unspecified error saving to database");
		}
	}

	private void setCurrentMonthForecastHours(ForecastHour forecastHours, List<Integer> allProjectForecastHours,
			List<Integer> allProjectFreq, List<Boolean> allWeekChk, List<Boolean> allWeekendChk,
			List<String> allComments) {

		LocalDate currentMonthStart = LocalDate.now().withDayOfMonth(1);

		forecastHours.setForecastHours1(allProjectForecastHours.get(0));
		forecastHours.setForecastHours2(allProjectForecastHours.get(1));
		forecastHours.setForecastHours3(allProjectForecastHours.get(2));
		forecastHours.setForecastHours4(allProjectForecastHours.get(3));
		forecastHours.setForecastHours5(allProjectForecastHours.get(4));
		forecastHours.setForecastHours6(allProjectForecastHours.get(5));
		forecastHours.setForecastHours7(allProjectForecastHours.get(6));
		forecastHours.setForecastHours8(allProjectForecastHours.get(7));
		forecastHours.setForecastHours9(allProjectForecastHours.get(8));
		forecastHours.setForecastHours10(allProjectForecastHours.get(9));
		forecastHours.setForecastHours11(allProjectForecastHours.get(10));
		forecastHours.setForecastHours12(allProjectForecastHours.get(11));
		forecastHours.setForecastHours13(allProjectForecastHours.get(12));
		forecastHours.setForecastHours14(allProjectForecastHours.get(13));

		forecastHours.setMonth1(currentMonthStart.minusMonths(1));
		forecastHours.setMonth2(currentMonthStart);
		forecastHours.setMonth3(currentMonthStart.plusMonths(1));
		forecastHours.setMonth4(currentMonthStart.plusMonths(2));
		forecastHours.setMonth5(currentMonthStart.plusMonths(3));
		forecastHours.setMonth6(currentMonthStart.plusMonths(4));
		forecastHours.setMonth7(currentMonthStart.plusMonths(5));
		forecastHours.setMonth8(currentMonthStart.plusMonths(6));
		forecastHours.setMonth9(currentMonthStart.plusMonths(7));
		forecastHours.setMonth10(currentMonthStart.plusMonths(8));
		forecastHours.setMonth11(currentMonthStart.plusMonths(9));
		forecastHours.setMonth12(currentMonthStart.plusMonths(10));
		forecastHours.setMonth13(currentMonthStart.plusMonths(11));
		forecastHours.setMonth14(currentMonthStart.plusMonths(12));

		forecastHours.setFreq1(allProjectFreq.get(0));
		forecastHours.setFreq2(allProjectFreq.get(1));
		forecastHours.setFreq3(allProjectFreq.get(2));
		forecastHours.setFreq4(allProjectFreq.get(3));
		forecastHours.setFreq5(allProjectFreq.get(4));
		forecastHours.setFreq6(allProjectFreq.get(5));
		forecastHours.setFreq7(allProjectFreq.get(6));
		forecastHours.setFreq8(allProjectFreq.get(7));
		forecastHours.setFreq9(allProjectFreq.get(8));
		forecastHours.setFreq10(allProjectFreq.get(9));
		forecastHours.setFreq11(allProjectFreq.get(10));
		forecastHours.setFreq12(allProjectFreq.get(11));
		forecastHours.setFreq13(allProjectFreq.get(12));
		forecastHours.setFreq14(allProjectFreq.get(13));

		forecastHours.setWeekchk1(allWeekChk.get(0));
		forecastHours.setWeekchk2(allWeekChk.get(1));
		forecastHours.setWeekchk3(allWeekChk.get(2));
		forecastHours.setWeekchk4(allWeekChk.get(3));
		forecastHours.setWeekchk5(allWeekChk.get(4));
		forecastHours.setWeekchk6(allWeekChk.get(5));
		forecastHours.setWeekchk7(allWeekChk.get(6));
		forecastHours.setWeekchk8(allWeekChk.get(7));
		forecastHours.setWeekchk9(allWeekChk.get(8));
		forecastHours.setWeekchk10(allWeekChk.get(9));
		forecastHours.setWeekchk11(allWeekChk.get(10));
		forecastHours.setWeekchk12(allWeekChk.get(11));
		forecastHours.setWeekchk13(allWeekChk.get(12));
		forecastHours.setWeekchk14(allWeekChk.get(13));

		forecastHours.setWeekendChk1(allWeekendChk.get(0));
		forecastHours.setWeekendChk2(allWeekendChk.get(1));
		forecastHours.setWeekendChk3(allWeekendChk.get(2));
		forecastHours.setWeekendChk4(allWeekendChk.get(3));
		forecastHours.setWeekendChk5(allWeekendChk.get(4));
		forecastHours.setWeekendChk6(allWeekendChk.get(5));
		forecastHours.setWeekendChk7(allWeekendChk.get(6));
		forecastHours.setWeekendChk8(allWeekendChk.get(7));
		forecastHours.setWeekendChk9(allWeekendChk.get(8));
		forecastHours.setWeekendChk10(allWeekendChk.get(9));
		forecastHours.setWeekendChk11(allWeekendChk.get(10));
		forecastHours.setWeekendChk12(allWeekendChk.get(11));
		forecastHours.setWeekendChk13(allWeekendChk.get(12));
		forecastHours.setWeekendChk14(allWeekendChk.get(13));

		forecastHours.setComment1(allComments.get(0));
		forecastHours.setComment2(allComments.get(1));
		forecastHours.setComment3(allComments.get(2));
		forecastHours.setComment4(allComments.get(3));
		forecastHours.setComment5(allComments.get(4));
		forecastHours.setComment6(allComments.get(5));
		forecastHours.setComment7(allComments.get(6));
		forecastHours.setComment8(allComments.get(7));
		forecastHours.setComment9(allComments.get(8));
		forecastHours.setComment10(allComments.get(9));
		forecastHours.setComment11(allComments.get(10));
		forecastHours.setComment12(allComments.get(11));
		forecastHours.setComment13(allComments.get(12));
		forecastHours.setComment14(allComments.get(13));
	}

	private ForecastHour initialForecastHours(int projectId) {
		// Get the current month start date
		LocalDate currentMonthStart = LocalDate.now().withDayOfMonth(1);

		// Create a new ForecastHour object with initial values using the constructor
		return new ForecastHour(0, projectId, currentMonthStart.minusMonths(1), currentMonthStart,
				currentMonthStart.plusMonths(1), currentMonthStart.plusMonths(2), currentMonthStart.plusMonths(3),
				currentMonthStart.plusMonths(4), currentMonthStart.plusMonths(5), currentMonthStart.plusMonths(6),
				currentMonthStart.plusMonths(7), currentMonthStart.plusMonths(8), currentMonthStart.plusMonths(9),
				currentMonthStart.plusMonths(10), currentMonthStart.plusMonths(11), currentMonthStart.plusMonths(12),
				"SYSTEM", LocalDate.now(), "SYSTEM", LocalDate.now());
	}
}
