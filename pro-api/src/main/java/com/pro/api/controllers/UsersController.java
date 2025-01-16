package com.pro.api.controllers;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.pro.api.models.business.AuthenticatedUser;
import com.pro.api.models.business.SessionUserEmail;
import com.pro.api.models.business.UserMin;
import com.pro.api.models.dataaccess.CoreHour;
import com.pro.api.models.dataaccess.CurrentUser;
import com.pro.api.models.dataaccess.InterviewerTimeCard;
import com.pro.api.models.dataaccess.Training;
import com.pro.api.models.dataaccess.User;
//import com.pro.api.models.dataaccess.repos.CoreHourRepository;
import com.pro.api.models.dataaccess.repos.CoreHourRepository;
import com.pro.api.models.dataaccess.repos.DropDownValueRepository;
import com.pro.api.models.dataaccess.repos.FormFieldRepository;
import com.pro.api.models.dataaccess.repos.InterviewerTimeCardRepository;
import com.pro.api.models.dataaccess.repos.TrainingRepository;
import com.pro.api.models.dataaccess.repos.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/users")
public class UsersController {
	private static final Logger logger = LoggerFactory.getLogger(UsersController.class);
	@Autowired
	private UserRepository userRepository;
	@Autowired
	DropDownValueRepository dvRepo;

	@Autowired
	FormFieldRepository formFieldRepo;

	@Autowired
	private CoreHourRepository coreHourRepository;
	@Autowired
	private TrainingRepository trainingRepository;
	@Autowired
	private InterviewerTimeCardRepository interviewerTimeCardRepository;

	@Autowired
	private SessionUserEmail UserEmail;

	@Value("${spring.profiles.active}")
	private String environment;

	@Value("${spring.profiles.active:}")
	private String activeProfile;

	@Value("${pro.api.GrouperAPIUrl}")
	private String GrouperAPIUrl;

	@Value("${pro.api.GrouperAPIUser}")
	private String GrouperAPIUser;

	@Value("${pro.api.GrouperAPIPassword}")
	private String GrouperAPIPassword;

	@ModelAttribute("UserEmail")
	public SessionUserEmail getUserEmail() {
		return UserEmail;
	}

	@GetMapping
	public GeneralResponse index() {
		GeneralResponse response = new GeneralResponse();
		try {

			response.Status = "Success";
			response.Message = "Successfully retrieved users";
			response.Subject = userRepository.findAll();
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	@GetMapping("/min")
	public GeneralResponse getUsersMin() {
		GeneralResponse response = new GeneralResponse();
		try {

			response.Status = "Success";
			response.Message = "Successfully retrieved users";
			response.Subject = userRepository.findAllUserMinsOrderedByName();
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	@GetMapping("/active")
	public GeneralResponse getActiveUsers() {
		GeneralResponse response = new GeneralResponse();
		try {

			response.Status = "Success";
			response.Message = "Successfully retrieved active users";
			response.Subject = userRepository.findAllActiveUsersOrderedByName();
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	@GetMapping("/activeIds")
	public GeneralResponse getActiveUserIds() {
		GeneralResponse response = new GeneralResponse();
		try {

			response.Status = "Success";
			response.Message = "Successfully retrieved active user ids";
			response.Subject = userRepository.findSortedActiveUserIds();
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	@GetMapping("/{netId}")
	public GeneralResponse getUser(@PathVariable String netId) {
		GeneralResponse response = new GeneralResponse();
		try {

			response.Status = "Success";
			response.Message = "Successfully retrieved user";
			response.Subject = userRepository.findFirstByDempoid(netId);
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	@GetMapping("/current/{netId}")
	public GeneralResponse getCurrentUser(@PathVariable String netId) {
		GeneralResponse response = new GeneralResponse();
		try {
			User user = userRepository.findFirstByDempoidIgnoreCase(netId);
			if (user == null) {
				response.Status = "Failure";
				response.Message = "user not found";
				return response;
			}
			List<InterviewerTimeCard> timecards = interviewerTimeCardRepository
					.findByDempoidIgnoreCaseOrderByDatetimeinDesc(netId);
			response.Status = "Success";
			response.Message = "Successfully retrieved current user";
			response.Subject = new CurrentUser(user, timecards);
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	@PostMapping
	public GeneralResponse saveUser(HttpServletRequest request, @RequestBody User user) {
		GeneralResponse response = new GeneralResponse();
		String netId = "Unknown";
		// Retrieve NetId from session if available
		Object netIdObj = request.getSession().getAttribute("NetId");
		if (netIdObj != null) {
			netId = (String) netIdObj;
		}
		boolean traininLogUpdated = false;

		try {
			traininLogUpdated = updateTrainingLog(user, netId);
			user = userRepository.save(user);
			response.Status = "Success";
			response.Message = "Successfully saved user";
			if (!traininLogUpdated)
				response.Message = "Successfully saved user, but the training log was not updated";
			response.Subject = user;
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	private boolean updateTrainingLog(User user, String netId) {
		if (netId == null || netId.isBlank()) {
			netId = "Unknown";
		}
		try {
			List<Training> currentTraining = trainingRepository.findByDempoidIgnoreCase(user.getDempoid());
			List<String> currentTrainingProjectIds = currentTraining.stream()
					.map(ct -> String.valueOf(ct.getProjectId())).collect(Collectors.toList());
			String trainedOn = user.getTrainedon();
			if ((trainedOn == null || trainedOn.isBlank()) && currentTraining.size() < 1) {
				return true;
			}
			List<String> trainedOnProjectIds = Arrays.stream(trainedOn.split("\\|")).collect(Collectors.toList());
			List<String> newTraining = trainedOnProjectIds.stream().filter(p -> !currentTrainingProjectIds.contains(p))
					.collect(Collectors.toList());
			List<Training> trainings = new ArrayList<Training>();

			for (String projectId : newTraining) {
				Training tr = new Training();
				tr.setDempoid(user.getDempoid());
				tr.setProjectId(Integer.parseInt(projectId));
				tr.setTrainingDate(new Date());
				tr.setEntryBy(netId);
				tr.setEntryDt(new Date());
				tr.setModBy(netId);
				tr.setModDt(new Date());
				tr.setUserId(0);
				trainings.add(tr);
			}

			// delete training
			List<Training> trainingToDelete = currentTraining.stream()
					.filter(t -> !trainedOnProjectIds.contains(Integer.toString(t.getProjectId())))
					.collect(Collectors.toList());
			try {
				trainingRepository.deleteAll(trainingToDelete);
				trainingRepository.saveAll(trainings);
			} catch (Exception ex) {
				throw new Exception(String.format("Could not save the following training for '%s': %s", netId,
						user.getTrainedon()));
			}
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	/*
	 * @GetMapping("/coreHours/{netId}") public GeneralResponse
	 * getUserCoreHours(@PathVariable String netId) { GeneralResponse response = new
	 * GeneralResponse(); try { CoreHour coreHours =
	 * coreHourRepository.findFirstByDempoid(netId); response.Status = "Success";
	 * response.Message = "Successfully retrieved core hours for " + netId;
	 * response.Subject = coreHours; } catch (Exception ex) { response.Status =
	 * "Failure"; response.Message = ex.getMessage(); }
	 * 
	 * return response; }
	 */

	/*
	 * @GetMapping("/corehours") public GeneralResponse getAllUserCoreHours() {
	 * GeneralResponse response = new GeneralResponse(); try { List<CoreHour>
	 * coreHours = coreHourRepository.findActiveCoreHours(); response.Status =
	 * "Success"; response.Message = "Successfully retrieved core hours";
	 * response.Subject = coreHours; } catch (Exception ex) { response.Status =
	 * "Failure"; response.Message = ex.getMessage(); }
	 * 
	 * return response; }
	 */

	/*
	 * @PostMapping("/coreHours") public GeneralResponse
	 * saveUserCoreHours(HttpServletRequest request, @RequestBody List<CoreHour>
	 * userCoreHours) { GeneralResponse response = new GeneralResponse(); String
	 * netId = "Unknown"; // Retrieve NetId from session if available Object
	 * netIdObj = request.getSession().getAttribute("NetId"); if (netIdObj != null)
	 * { netId = (String) netIdObj; } List<CoreHour> savedCoreHours = new
	 * ArrayList<CoreHour>(); List<String> errorMessages = new ArrayList<String>();
	 * try { for (CoreHour coreHours : userCoreHours) { try { try { CoreHour ch =
	 * coreHourRepository.save(coreHours); savedCoreHours.add(ch); } catch
	 * (Exception ex) { throw new Exception("Unspecified error saving to database");
	 * } } catch (Exception ex) { errorMessages.add(ex.getMessage()); } }
	 * response.Status = "Success"; response.Message =
	 * "Successfully saved core hours"; response.Subject = savedCoreHours; if
	 * (errorMessages.size() > 0) { response.Message =
	 * String.format("%d core hours saved, %d core hours failed to save:\n%s",
	 * savedCoreHours.size(), errorMessages.size(), String.join("\n",
	 * errorMessages)); } if (savedCoreHours.size() < 1) { throw new
	 * Exception("No core hours were saved in the database"); } } catch (Exception
	 * ex) { response.Status = "Failure"; response.Message = ex.getMessage(); }
	 * 
	 * return response; }
	 */

	/*
	 * @GetMapping("/coreHours/reconcile") public GeneralResponse
	 * reconcileCoreHours() { GeneralResponse response = new GeneralResponse(); try
	 * { List<CoreHour> coreHours = coreHourRepository.findAll(); LocalDate
	 * currentMonthStart = LocalDate.now().withDayOfMonth(1); if (coreHours.size() <
	 * 1) { setInitialCoreHoursAll(); coreHours = coreHourRepository.findAll(); } if
	 * (coreHourRepository.hasFilteredCoreHours(currentMonthStart)) {
	 * coreHoursReconciliation(true); // reconcile inactive users as well, but do
	 * them separately coreHoursReconciliation(false);
	 * 
	 * response.Status = "Success"; response.Message =
	 * "Successfully reconciled core hours"; } else { response.Status = "Success";
	 * response.Message = "Hours already reconciled"; } } catch (Exception ex) {
	 * response.Status = "Failure"; response.Message = ex.getMessage(); }
	 * 
	 * return response; }
	 */

	@PostMapping("/saveActiveAndLock")
	public GeneralResponse saveActiveAndLockStatus(HttpServletRequest request, @RequestBody List<UserMin> usersMin) {
		GeneralResponse response = new GeneralResponse();
		String netId = "Unknown";
		// Retrieve NetId from session if available
		Object netIdObj = request.getSession().getAttribute("NetId");
		if (netIdObj != null) {
			netId = (String) netIdObj;
		}

		try {
			for (UserMin userMin : usersMin) {
				Optional<User> optionalUser = userRepository.findById(userMin.getUserid());
				User user = optionalUser.orElseGet(User::new);
//				user.setStatus(userMin.getStatus());
				user.setCanedit(userMin.getCanEdit());
				try {
					userRepository.save(user);
				} catch (Exception ex) {
					throw new Exception("Unspecified error saving to database");
				}
				response.Status = "Success";
				response.Message = "Successfully saved users";
			}
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	@PostMapping("/timecards")
	public GeneralResponse timeInOut(HttpServletRequest request, @RequestBody InterviewerTimeCard timecard) {
		GeneralResponse response = new GeneralResponse();
		String netId = "Unknown";
		// Retrieve NetId from session if available
		Object netIdObj = request.getSession().getAttribute("NetId");
		if (netIdObj != null) {
			netId = (String) netIdObj;
		}

		try {
			try {
				timecard = interviewerTimeCardRepository.save(timecard);
			} catch (Exception ex) {
				throw new Exception("Unspecified error saving to database");
			}
			response.Status = "Success";
			response.Message = "Successfully saved timecard";
			response.Subject = timecard;
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	// ----------------------------------
	// helper methods
	// ----------------------------------
	/*
	 * private void setInitialCoreHoursAll() throws Exception { LocalDate
	 * currentMonthStart = LocalDate.now().withDayOfMonth(1); List<User> users =
	 * userRepository.findAll(); try { for (User user : users) { CoreHour
	 * userCoreHours = initialCoreHours(user.getDempoid(), user.getCorehours());
	 * coreHourRepository.save(userCoreHours); } } catch (Exception ex) { throw new
	 * Exception("Unspecified error saving to database"); } }
	 */

	/*
	 * private void coreHoursReconciliation(Integer status) throws Exception {
	 * LocalDate currentMonthStart = LocalDate.now().withDayOfMonth(1); try {
	 * List<User> users = userRepository.findByStatus(status); for (User user :
	 * users) { CoreHour userCoreHours =
	 * coreHourRepository.findFirstByDempoid(user.getDempoid()); if (userCoreHours
	 * == null) { userCoreHours = initialCoreHours(user.getDempoid(),
	 * user.getCorehours()); } else { List<CoreHour> userCoreHoursMulti =
	 * coreHourRepository .findAllByDempoidAndCoreHoursIdNot(user.getDempoid(),
	 * userCoreHours.getCoreHoursId()); // delete any extra core hours entries if
	 * found if (!userCoreHoursMulti.isEmpty()) {
	 * coreHourRepository.deleteAll(userCoreHoursMulti); } } // if modified if
	 * (userCoreHours.getCoreHoursId() > 0) { // check if dates are correct or not
	 * // default to 13 in case we don't have dates int dateDifference = 13;
	 * 
	 * // if Month2 does not have a value, we can't calculate the date difference,
	 * so // use the default of 13 to perform the > 12 month logic to reset if
	 * (userCoreHours.getMonth2() != null) { dateDifference =
	 * ((currentMonthStart.getYear() - userCoreHours.getMonth2().getYear()) * 12) +
	 * (currentMonthStart.getMonthValue() -
	 * userCoreHours.getMonth2().getMonthValue()); } List<Integer> allUserCoreHours
	 * = Arrays.asList(userCoreHours.getCoreHours1(), userCoreHours.getCoreHours2(),
	 * userCoreHours.getCoreHours3(), userCoreHours.getCoreHours4(),
	 * userCoreHours.getCoreHours5(), userCoreHours.getCoreHours6(),
	 * userCoreHours.getCoreHours7(), userCoreHours.getCoreHours8(),
	 * userCoreHours.getCoreHours9(), userCoreHours.getCoreHours10(),
	 * userCoreHours.getCoreHours11(), userCoreHours.getCoreHours12(),
	 * userCoreHours.getCoreHours13(), userCoreHours.getCoreHours14()); if
	 * (dateDifference > 0) { Integer lastCoreHours = allUserCoreHours.get(13); if
	 * (dateDifference <= 12) { // Get the last element // whatever the difference
	 * in months, remove that number of items from the // beginning allUserCoreHours
	 * = new
	 * ArrayList<>(allUserCoreHours.subList(dateDifference,allUserCoreHours.size()))
	 * ; for (int i = 0; i < dateDifference; i++) {
	 * allUserCoreHours.add(lastCoreHours); } } if (dateDifference > 12) {
	 * allUserCoreHours = Arrays.asList(lastCoreHours, lastCoreHours, lastCoreHours,
	 * lastCoreHours, lastCoreHours, lastCoreHours, lastCoreHours, lastCoreHours,
	 * lastCoreHours, lastCoreHours, lastCoreHours, lastCoreHours, lastCoreHours,
	 * lastCoreHours); } setCurrentMonthCoreHours(userCoreHours, allUserCoreHours);
	 * } userCoreHours.setModBy("SYSTEM"); userCoreHours.setModDt(LocalDate.now());
	 * } coreHourRepository.save(userCoreHours); } } catch (Exception ex) { throw
	 * new Exception("Unspecified error saving to database"); } }
	 * 
	 * private void setCurrentMonthCoreHours(CoreHour coreHour, List<Integer>
	 * allUserCoreHours) { LocalDate currentMonthStart =
	 * LocalDate.now().withDayOfMonth(1);
	 * 
	 * coreHour.setCoreHours1(allUserCoreHours.get(0));
	 * coreHour.setCoreHours2(allUserCoreHours.get(1));
	 * coreHour.setCoreHours3(allUserCoreHours.get(2));
	 * coreHour.setCoreHours4(allUserCoreHours.get(3));
	 * coreHour.setCoreHours5(allUserCoreHours.get(4));
	 * coreHour.setCoreHours6(allUserCoreHours.get(5));
	 * coreHour.setCoreHours7(allUserCoreHours.get(6));
	 * coreHour.setCoreHours8(allUserCoreHours.get(7));
	 * coreHour.setCoreHours9(allUserCoreHours.get(8));
	 * coreHour.setCoreHours10(allUserCoreHours.get(9));
	 * coreHour.setCoreHours11(allUserCoreHours.get(10));
	 * coreHour.setCoreHours12(allUserCoreHours.get(11));
	 * coreHour.setCoreHours13(allUserCoreHours.get(12));
	 * coreHour.setCoreHours14(allUserCoreHours.get(13));
	 * 
	 * coreHour.setMonth1(currentMonthStart.minusMonths(1));
	 * coreHour.setMonth2(currentMonthStart);
	 * coreHour.setMonth3(currentMonthStart.plusMonths(1));
	 * coreHour.setMonth4(currentMonthStart.plusMonths(2));
	 * coreHour.setMonth5(currentMonthStart.plusMonths(3));
	 * coreHour.setMonth6(currentMonthStart.plusMonths(4));
	 * coreHour.setMonth7(currentMonthStart.plusMonths(5));
	 * coreHour.setMonth8(currentMonthStart.plusMonths(6));
	 * coreHour.setMonth9(currentMonthStart.plusMonths(7));
	 * coreHour.setMonth10(currentMonthStart.plusMonths(8));
	 * coreHour.setMonth11(currentMonthStart.plusMonths(9));
	 * coreHour.setMonth12(currentMonthStart.plusMonths(10));
	 * coreHour.setMonth13(currentMonthStart.plusMonths(11));
	 * coreHour.setMonth14(currentMonthStart.plusMonths(12)); }
	 * 
	 * private CoreHour initialCoreHours(String dempoid, int coreHours) { LocalDate
	 * currentDate = LocalDate.now(); LocalDate currentMonthStart =
	 * LocalDate.of(currentDate.getYear(), currentDate.getMonth(), 1);
	 * 
	 * if (coreHours < 1) { coreHours = 0; }
	 * 
	 * return new CoreHour(0, dempoid, coreHours, coreHours, coreHours, coreHours,
	 * coreHours, coreHours, coreHours, coreHours, coreHours, coreHours, coreHours,
	 * coreHours, coreHours, coreHours, currentMonthStart.minusMonths(1),
	 * currentMonthStart, currentMonthStart.plusMonths(1),
	 * currentMonthStart.plusMonths(2), currentMonthStart.plusMonths(3),
	 * currentMonthStart.plusMonths(4), currentMonthStart.plusMonths(5),
	 * currentMonthStart.plusMonths(6), currentMonthStart.plusMonths(7),
	 * currentMonthStart.plusMonths(8), currentMonthStart.plusMonths(9),
	 * currentMonthStart.plusMonths(10), currentMonthStart.plusMonths(11),
	 * currentMonthStart.plusMonths(12), "SYSTEM", currentDate, "SYSTEM",
	 * currentDate); }
	 */

	@GetMapping("/test")
	public GeneralResponse testResponse() {
		GeneralResponse response = new GeneralResponse();
		try {
			DirContext dctxt = getUserDetails("0731702");
			response.Status = "Success";
			response.Message = "Successfully retrieved users";
			response.Subject = dctxt;
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	public DirContext getUserDetails(String duid) {

		Hashtable<String, String> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, "ldap://ldap.duke.edu/ou=people,dc=duke,dc=edu");
		env.put(Context.SECURITY_AUTHENTICATION, "none");
		DirContext ctx = null;

		try {
			ctx = new InitialDirContext(env);
			SearchControls controls = new SearchControls();
			controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			NamingEnumeration<SearchResult> results = ctx.search("", "(dudukeid=" + duid + ")", controls);
			if (results.hasMore()) {
				SearchResult searchResult = results.next();
				return (DirContext) searchResult.getObject();
			}
		} catch (NamingException e) {
			System.out.println(duid + " failed user details");
			e.printStackTrace();
		}
		return null;

	}

	// POST: /api/users/current
	@PostMapping("current")
	public GeneralResponse userLoginInfo(HttpSession httpSession, ModelMap model) {
		GeneralResponse response = new GeneralResponse();

		AuthenticatedUser authenticatedUser = new AuthenticatedUser();

		// String emailAddress = (httpSession.getAttribute("UserEmail") == null ? "" :
		// httpSession.getAttribute("UserEmail").toString());
		String emailAddress = "";

		// uncomment below to use default authenticated user, including default, spoofed
		// grouper assignments - if running locally, return a default user
		if (activeProfile.equals("local")) {
			getDefaultAuthenticatedUser(authenticatedUser);

			response.Status = "Success";
			response.Message = "Successfully retrieved login info";
			response.Subject = authenticatedUser;

			return response;
		}

		// uncomment below if you just want to spoof a user by email - if running
		// locally, force lookup of specific user by email address
		// if (activeProfile.equals("local")) {
		// emailAddress = "";
		// }

		if (UserEmail != null && !activeProfile.equals("local")) {
			emailAddress = UserEmail.getUserEmail();
		}

		if (emailAddress == null || emailAddress.trim().isEmpty()) {
//			throw new Exception("Could not find a valid session");
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Could not find a valid session");
		}

		try {

			User user = userRepository.findFirstByEmailaddrIgnoreCase(emailAddress);
			int roleffid = formFieldRepo.findFormFieldByFieldLabel("Role");
			String role = dvRepo.findFirstByFormFieldIdAndCodeValues(roleffid, user.getRole());
			logger.info("user role for user {} {} is {}", user.getFname(), user.getLname(), role);
			authenticatedUser = new AuthenticatedUser(user);

			if (role != null && role.trim().equals("Admin")) {
				authenticatedUser.setAdmin(true);
			} else if (role != null && role.trim().equals("Interviewer")) {
				authenticatedUser.setInterviewer(true);
			} else if (role != null && role.trim().equals("Project Team")) {
				authenticatedUser.setProjectTeam(true);
			} else if (role != null && role.trim().equals("Outcomes IT")) {
				authenticatedUser.setOutcomesIt(true);
			}

			// GET
			/*
			 * HttpClient client = HttpClient.newHttpClient();
			 * 
			 * // authorization header String authenticationString = GrouperAPIUser + ":" +
			 * GrouperAPIPassword; String base64EncodedAuthenticationString = new String(
			 * Base64.encodeBase64String(authenticationString.getBytes())); String
			 * grouperUrl = String.format("%s/subjects/%s/groups", GrouperAPIUrl,
			 * authenticatedUser.getDuDukeID()); HttpRequest grouperRequest =
			 * HttpRequest.newBuilder() .uri(URI.create(grouperUrl))
			 * .headers("Authorization", "Basic " + base64EncodedAuthenticationString)
			 * .GET() .build();
			 * 
			 * HttpResponse<String> grouperResponse = client.send(grouperRequest,
			 * HttpResponse.BodyHandlers.ofString());
			 */
			// log the status code for good responses, and log the body for all other
			// responses
			/*
			 * if (grouperResponse.statusCode() >= 200 && grouperResponse.statusCode() <
			 * 300) {
			 * 
			 * String environmentPostFix = "-" + environment; if
			 * (activeProfile.equals("local")) { environmentPostFix = "-dev"; } String[]
			 * validGroupNames = { String.format("ccep-admin%s", environmentPostFix),
			 * String.format("ccep-interviewer%s", environmentPostFix),
			 * String.format("ccep-resourcegroup%s", environmentPostFix) };
			 * 
			 * String responseBody = grouperResponse.body(); ArrayList<String> userRoles =
			 * new ArrayList<>();
			 */
			/*
			 * for (String groupName : validGroupNames) { if
			 * (responseBody.contains(groupName)) {
			 * 
			 * if (groupName.contains("admin")) { authenticatedUser.setAdmin(true); }
			 * 
			 * if (groupName.contains("interviewer")) {
			 * authenticatedUser.setInterviewer(true); }
			 * 
			 * if (groupName.contains("resourcegroup")) {
			 * authenticatedUser.setResourceGroup(true); }
			 * 
			 * // Remove environmentPostFix from the end of the groupName if it exists if
			 * (groupName.endsWith(environmentPostFix)) { groupName = groupName.substring(0,
			 * groupName.length() - environmentPostFix.length()); }
			 * 
			 * userRoles.add(groupName); } }
			 * authenticatedUser.setUserRoles(userRoles.toArray(new String[0]));
			 * 
			 * response.Message = "Successfully retrieved login info";
			 * logger.info("Successfully retrieved login info {}",authenticatedUser.
			 * getDisplayName()); } else { //error response.Message = String.format("{0}",
			 * grouperResponse.body()); }
			 */

			response.Status = "Success";
			response.Subject = authenticatedUser;
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	private void getDefaultAuthenticatedUser(AuthenticatedUser user) {
		user.setEppn("jeremiah.reed@duke.edu");
		user.setNetID("jmr110");
		user.setDisplayName("duke");

		// set the below as needed for testing locally
		user.interviewer = false;
		user.resourceGroup = false;
		user.admin = true;
		// user.resourceGroup = false;
		// user.admin = false;

	}

	// DELETE: /api/users/Current
	@DeleteMapping("current")
	public GeneralResponse destroySession(HttpSession httpSession, ModelMap model) {
		GeneralResponse response = new GeneralResponse();

		try {
			httpSession.invalidate();

			response.Status = "Success";
			response.Message = "Successfully logged out";
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	@GetMapping("/coreHours/{netId}")
	public GeneralResponse getUserCoreHours(@PathVariable String netId) {
		GeneralResponse response = new GeneralResponse();
		try {
			CoreHour coreHours = coreHourRepository.findFirstByDempoid(netId);
			response.Status = "Success";
			response.Message = "Successfully retrieved core hours for " + netId;
			response.Subject = coreHours;
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	@GetMapping("/coreHours-V2/{netId}")
	public GeneralResponse getUserCoreHoursV2(@PathVariable String netId) {
		GeneralResponse response = new GeneralResponse();
		try {
			CoreHour coreHours = coreHourRepository.findFirstByDempoid(netId);
			System.out.println("coreHours---" + coreHours);

			Map<String, String> monthMap = new HashMap<>();

			try {
				Field[] fields = CoreHour.class.getDeclaredFields();

				int currentYear = LocalDate.now().getYear();

				for (Field field : fields) {
					if (field.getName().startsWith("month")) {
						field.setAccessible(true);

						LocalDate fieldValue = (LocalDate) field.get(coreHours);

						if (fieldValue != null && fieldValue.getYear() >= currentYear) {
							int monthValue = fieldValue.getMonthValue();
							int year = fieldValue.getYear();
							monthMap.put(monthValue + "" + year, field.getName());
						}
					}
				}

				System.out.println("Month Map: " + monthMap);
			} catch (IllegalAccessException e) {
				System.err.println("Error accessing fields: " + e.getMessage());
			}

			Map<String, Integer> coreHoursMap = new HashMap<>();
			coreHoursMap.put("coreHours1", coreHours.getCoreHours1());
			coreHoursMap.put("coreHours2", coreHours.getCoreHours2());
			coreHoursMap.put("coreHours3", coreHours.getCoreHours3());
			coreHoursMap.put("coreHours4", coreHours.getCoreHours4());
			coreHoursMap.put("coreHours5", coreHours.getCoreHours5());
			coreHoursMap.put("coreHours6", coreHours.getCoreHours6());
			coreHoursMap.put("coreHours7", coreHours.getCoreHours7());
			coreHoursMap.put("coreHours8", coreHours.getCoreHours8());
			coreHoursMap.put("coreHours9", coreHours.getCoreHours9());
			coreHoursMap.put("coreHours10", coreHours.getCoreHours10());
			coreHoursMap.put("coreHours11", coreHours.getCoreHours11());
			coreHoursMap.put("coreHours12", coreHours.getCoreHours12());
			coreHoursMap.put("coreHours13", coreHours.getCoreHours13());
			coreHoursMap.put("coreHours14", coreHours.getCoreHours14());

			List<LocalDate> dateList = new ArrayList<>();

			LocalDate currentDate = LocalDate.now().withDayOfMonth(1);

			for (int i = 0; i <= 13; i++) {
				dateList.add(currentDate.plusMonths(i));
			}

			for (int i = 0; i < dateList.size(); i++) {
				int monthValue = dateList.get(i).getMonthValue();
				int year = dateList.get(i).getYear();
				String monthName = monthMap.get(monthValue + "" + year);
						
				if (monthName != null) {
					String val = getVal(monthName);
					Integer coreHoursValue = coreHoursMap.get(val);

					if (coreHoursValue != null) {
						switch (i) {
						case 0:
							coreHours.setMonth1(dateList.get(i));
							coreHours.setCoreHours1(coreHoursValue);
							break;
						case 1:
							coreHours.setMonth2(dateList.get(i));
							coreHours.setCoreHours2(coreHoursValue);
							break;
						case 2:
							coreHours.setMonth3(dateList.get(i));
							coreHours.setCoreHours3(coreHoursValue);
							break;
						case 3:
							coreHours.setMonth4(dateList.get(i));
							coreHours.setCoreHours4(coreHoursValue);
							break;
						case 4:
							coreHours.setMonth5(dateList.get(i));
							coreHours.setCoreHours5(coreHoursValue);
							break;
						case 5:
							coreHours.setMonth6(dateList.get(i));
							coreHours.setCoreHours6(coreHoursValue);
							break;
						case 6:
							coreHours.setMonth7(dateList.get(i));
							coreHours.setCoreHours7(coreHoursValue);
							break;
						case 7:
							coreHours.setMonth8(dateList.get(i));
							coreHours.setCoreHours8(coreHoursValue);
							break;
						case 8:
							coreHours.setMonth9(dateList.get(i));
							coreHours.setCoreHours9(coreHoursValue);
							break;
						case 9:
							coreHours.setMonth10(dateList.get(i));
							coreHours.setCoreHours10(coreHoursValue);
							break;
						case 10:
							coreHours.setMonth11(dateList.get(i));
							coreHours.setCoreHours11(coreHoursValue);
							break;
						case 11:
							coreHours.setMonth12(dateList.get(i));
							coreHours.setCoreHours12(coreHoursValue);
							break;
						case 12:
							coreHours.setMonth13(dateList.get(i));
							coreHours.setCoreHours13(coreHoursValue);
							break;
						case 13:
							coreHours.setMonth14(dateList.get(i));
							coreHours.setCoreHours14(coreHoursValue);
							break;
						default:
							break;
						}
					}
				}else {
					switch (i) {
					case 0:
						coreHours.setMonth1(dateList.get(i));
						coreHours.setCoreHours1(0);
						break;
					case 1:
						coreHours.setMonth2(dateList.get(i));
						coreHours.setCoreHours2(0);
						break;
					case 2:
						coreHours.setMonth3(dateList.get(i));
						coreHours.setCoreHours3(0);
						break;
					case 3:
						coreHours.setMonth4(dateList.get(i));
						coreHours.setCoreHours4(0);
						break;
					case 4:
						coreHours.setMonth5(dateList.get(i));
						coreHours.setCoreHours5(0);
						break;
					case 5:
						coreHours.setMonth6(dateList.get(i));
						coreHours.setCoreHours6(0);
						break;
					case 6:
						coreHours.setMonth7(dateList.get(i));
						coreHours.setCoreHours7(0);
						break;
					case 7:
						coreHours.setMonth8(dateList.get(i));
						coreHours.setCoreHours8(0);
						break;
					case 8:
						coreHours.setMonth9(dateList.get(i));
						coreHours.setCoreHours9(0);
						break;
					case 9:
						coreHours.setMonth10(dateList.get(i));
						coreHours.setCoreHours10(0);
						break;
					case 10:
						coreHours.setMonth11(dateList.get(i));
						coreHours.setCoreHours11(0);
						break;
					case 11:
						coreHours.setMonth12(dateList.get(i));
						coreHours.setCoreHours12(0);
						break;
					case 12:
						coreHours.setMonth13(dateList.get(i));
						coreHours.setCoreHours13(0);
						break;
					case 13:
						coreHours.setMonth14(dateList.get(i));
						coreHours.setCoreHours14(0);
						break;
					default:
						break;
					}	
				}
			}

			System.out.println("dateList---" + dateList);
			response.Status = "Success";
			response.Message = "Successfully retrieved core hours for " + netId;
			response.Subject = coreHours;
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	public String getVal(String val) {
		if (val == null) {
			return ""; // Return an empty string if val is null
		}
		switch (val) {
		case "month1":
			return "coreHours1";
		case "month2":
			return "coreHours2";
		case "month3":
			return "coreHours3";
		case "month4":
			return "coreHours4";
		case "month5":
			return "coreHours5";
		case "month6":
			return "coreHours6";
		case "month7":
			return "coreHours7";
		case "month8":
			return "coreHours8";
		case "month9":
			return "coreHours9";
		case "month10":
			return "coreHours10";
		case "month11":
			return "coreHours11";
		case "month12":
			return "coreHours12";
		case "month13":
			return "coreHours13";
		case "month14":
			return "coreHours14";
		default:
			return "";
		}
	}

	@GetMapping("/corehours")
	public GeneralResponse getAllUserCoreHours() {
		GeneralResponse response = new GeneralResponse();
		try {
			List<CoreHour> coreHours = coreHourRepository.findActiveCoreHours();
			response.Status = "Success";
			response.Message = "Successfully retrieved core hours";
			response.Subject = coreHours;
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	@PostMapping("/coreHours")
	public GeneralResponse saveUserCoreHours(HttpServletRequest request, @RequestBody List<CoreHour> userCoreHours) {
		GeneralResponse response = new GeneralResponse();
		String netId = "Unknown";
		// Retrieve NetId from session if available
		Object netIdObj = request.getSession().getAttribute("NetId");
		if (netIdObj != null) {
			netId = (String) netIdObj;
		}
		List<CoreHour> savedCoreHours = new ArrayList<CoreHour>();
		List<String> errorMessages = new ArrayList<String>();
		try {
			for (CoreHour coreHours : userCoreHours) {
				try {
					try {
						CoreHour ch = coreHourRepository.save(coreHours);
						savedCoreHours.add(ch);
					} catch (Exception ex) {
						throw new Exception("Unspecified error saving to database");
					}
				} catch (Exception ex) {
					errorMessages.add(ex.getMessage());
				}
			}
			response.Status = "Success";
			response.Message = "Successfully saved core hours";
			response.Subject = savedCoreHours;
			if (errorMessages.size() > 0) {
				response.Message = String.format("%d core hours saved, %d core hours failed to save:\n%s",
						savedCoreHours.size(), errorMessages.size(), String.join("\n", errorMessages));
			}
			if (savedCoreHours.size() < 1) {
				throw new Exception("No core hours were saved in the database");
			}
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	@GetMapping("/coreHours/reconcile")
	public GeneralResponse reconcileCoreHours() {
		GeneralResponse response = new GeneralResponse();
		try {
			List<CoreHour> coreHours = coreHourRepository.findAll();
			LocalDate currentMonthStart = LocalDate.now().withDayOfMonth(1);
			if (coreHours.size() < 1) {
				setInitialCoreHoursAll();
				coreHours = coreHourRepository.findAll();
			}
			if (coreHourRepository.hasFilteredCoreHours(currentMonthStart)) {
				coreHoursReconciliation(true);
				// reconcile inactive users as well, but do them separately
				coreHoursReconciliation(false);

				response.Status = "Success";
				response.Message = "Successfully reconciled core hours";
			} else {
				response.Status = "Success";
				response.Message = "Hours already reconciled";
			}
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	private void setInitialCoreHoursAll() throws Exception {
		LocalDate currentMonthStart = LocalDate.now().withDayOfMonth(1);
		List<User> users = userRepository.findAll();
		try {
			for (User user : users) {
				CoreHour userCoreHours = initialCoreHours(user.getDempoid(), user.getCorehours());
				coreHourRepository.save(userCoreHours);
			}
		} catch (Exception ex) {
			throw new Exception("Unspecified error saving to database");
		}
	}

	private void coreHoursReconciliation(boolean active) throws Exception {
		LocalDate currentMonthStart = LocalDate.now().withDayOfMonth(1);
		try {
			List<User> users = userRepository.findByStatus(active ? 1 : 0);
			for (User user : users) {
				CoreHour userCoreHours = coreHourRepository.findFirstByDempoid(user.getDempoid());
				if (userCoreHours == null) {
					userCoreHours = initialCoreHours(user.getDempoid(), user.getCorehours());
				} else {
					List<CoreHour> userCoreHoursMulti = coreHourRepository
							.findAllByDempoidAndCoreHoursIdNot(user.getDempoid(), userCoreHours.getCoreHoursId());
					// delete any extra core hours entries if found
					if (!userCoreHoursMulti.isEmpty()) {
						coreHourRepository.deleteAll(userCoreHoursMulti);
					}
				}
				// if modified
				if (userCoreHours.getCoreHoursId() > 0) {
					// check if dates are correct or not
					// default to 13 in case we don't have dates
					int dateDifference = 13;

					// if Month2 does not have a value, we can't calculate the date difference, so
					// use the default of 13 to perform the > 12 month logic to reset
					if (userCoreHours.getMonth2() != null) {
						dateDifference = ((currentMonthStart.getYear() - userCoreHours.getMonth2().getYear()) * 12)
								+ (currentMonthStart.getMonthValue() - userCoreHours.getMonth2().getMonthValue());
					}
					List<Integer> allUserCoreHours = Arrays.asList(userCoreHours.getCoreHours1(),
							userCoreHours.getCoreHours2(), userCoreHours.getCoreHours3(), userCoreHours.getCoreHours4(),
							userCoreHours.getCoreHours5(), userCoreHours.getCoreHours6(), userCoreHours.getCoreHours7(),
							userCoreHours.getCoreHours8(), userCoreHours.getCoreHours9(),
							userCoreHours.getCoreHours10(), userCoreHours.getCoreHours11(),
							userCoreHours.getCoreHours12(), userCoreHours.getCoreHours13(),
							userCoreHours.getCoreHours14());
					if (dateDifference > 0) {
						Integer lastCoreHours = allUserCoreHours.get(13);
						if (dateDifference <= 12) {
							// Get the last element
							// whatever the difference in months, remove that number of items from the
							// beginning
							allUserCoreHours = new ArrayList<>(
									allUserCoreHours.subList(dateDifference, allUserCoreHours.size()));
							for (int i = 0; i < dateDifference; i++) {
								allUserCoreHours.add(lastCoreHours);
							}
						}
						if (dateDifference > 12) {
							allUserCoreHours = Arrays.asList(lastCoreHours, lastCoreHours, lastCoreHours, lastCoreHours,
									lastCoreHours, lastCoreHours, lastCoreHours, lastCoreHours, lastCoreHours,
									lastCoreHours, lastCoreHours, lastCoreHours, lastCoreHours, lastCoreHours);
						}
						setCurrentMonthCoreHours(userCoreHours, allUserCoreHours);
					}
					userCoreHours.setModBy("SYSTEM");
					userCoreHours.setModDt(LocalDate.now());
				}
				coreHourRepository.save(userCoreHours);
			}
		} catch (Exception ex) {
			throw new Exception("Unspecified error saving to database");
		}
	}

	private void setCurrentMonthCoreHours(CoreHour coreHour, List<Integer> allUserCoreHours) {
		LocalDate currentMonthStart = LocalDate.now().withDayOfMonth(1);

		coreHour.setCoreHours1(allUserCoreHours.get(0));
		coreHour.setCoreHours2(allUserCoreHours.get(1));
		coreHour.setCoreHours3(allUserCoreHours.get(2));
		coreHour.setCoreHours4(allUserCoreHours.get(3));
		coreHour.setCoreHours5(allUserCoreHours.get(4));
		coreHour.setCoreHours6(allUserCoreHours.get(5));
		coreHour.setCoreHours7(allUserCoreHours.get(6));
		coreHour.setCoreHours8(allUserCoreHours.get(7));
		coreHour.setCoreHours9(allUserCoreHours.get(8));
		coreHour.setCoreHours10(allUserCoreHours.get(9));
		coreHour.setCoreHours11(allUserCoreHours.get(10));
		coreHour.setCoreHours12(allUserCoreHours.get(11));
		coreHour.setCoreHours13(allUserCoreHours.get(12));
		coreHour.setCoreHours14(allUserCoreHours.get(13));

		coreHour.setMonth1(currentMonthStart.minusMonths(1));
		coreHour.setMonth2(currentMonthStart);
		coreHour.setMonth3(currentMonthStart.plusMonths(1));
		coreHour.setMonth4(currentMonthStart.plusMonths(2));
		coreHour.setMonth5(currentMonthStart.plusMonths(3));
		coreHour.setMonth6(currentMonthStart.plusMonths(4));
		coreHour.setMonth7(currentMonthStart.plusMonths(5));
		coreHour.setMonth8(currentMonthStart.plusMonths(6));
		coreHour.setMonth9(currentMonthStart.plusMonths(7));
		coreHour.setMonth10(currentMonthStart.plusMonths(8));
		coreHour.setMonth11(currentMonthStart.plusMonths(9));
		coreHour.setMonth12(currentMonthStart.plusMonths(10));
		coreHour.setMonth13(currentMonthStart.plusMonths(11));
		coreHour.setMonth14(currentMonthStart.plusMonths(12));
	}

	private CoreHour initialCoreHours(String dempoid, int coreHours) {
		LocalDate currentDate = LocalDate.now();
		LocalDate currentMonthStart = LocalDate.of(currentDate.getYear(), currentDate.getMonth(), 1);

		if (coreHours < 1) {
			coreHours = 0;
		}

		return new CoreHour(0, dempoid, coreHours, coreHours, coreHours, coreHours, coreHours, coreHours, coreHours,
				coreHours, coreHours, coreHours, coreHours, coreHours, coreHours, coreHours,
				currentMonthStart.minusMonths(1), currentMonthStart, currentMonthStart.plusMonths(1),
				currentMonthStart.plusMonths(2), currentMonthStart.plusMonths(3), currentMonthStart.plusMonths(4),
				currentMonthStart.plusMonths(5), currentMonthStart.plusMonths(6), currentMonthStart.plusMonths(7),
				currentMonthStart.plusMonths(8), currentMonthStart.plusMonths(9), currentMonthStart.plusMonths(10),
				currentMonthStart.plusMonths(11), currentMonthStart.plusMonths(12), "SYSTEM", currentDate, "SYSTEM",
				currentDate);
	}

}
