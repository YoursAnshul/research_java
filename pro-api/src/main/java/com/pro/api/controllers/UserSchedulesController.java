package com.pro.api.controllers;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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

import com.pro.api.models.business.KeyValuePair;
import com.pro.api.models.business.SessionUserEmail;
import com.pro.api.models.business.ValidationMessagePlus;
import com.pro.api.models.dataaccess.CoreHour;
import com.pro.api.models.dataaccess.Request;
import com.pro.api.models.dataaccess.Schedule;
import com.pro.api.models.dataaccess.User;
import com.pro.api.models.dataaccess.ValidationMessage;
import com.pro.api.models.dataaccess.ViUserSchedule;
import com.pro.api.models.dataaccess.repos.CoreHourRepository;
import com.pro.api.models.dataaccess.repos.RequestRepository;
import com.pro.api.models.dataaccess.repos.ScheduleRepository;
import com.pro.api.models.dataaccess.repos.TimeCodeRepository;
import com.pro.api.models.dataaccess.repos.UserRepository;
import com.pro.api.models.dataaccess.repos.ValidationMessageRepository;
import com.pro.api.models.dataaccess.repos.ValidationMessageTextRepository;
import com.pro.api.models.dataaccess.repos.ViUserScheduleRepository;
import com.pro.api.response.ScheduleResponse;
import com.pro.api.response.ShiftScheduleRequest;
import com.pro.api.service.AuditService;
import com.pro.api.service.ScheduleService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/userSchedules")
public class UserSchedulesController {
	private static final Logger logger = LoggerFactory.getLogger(UserSchedulesController.class);

	@Autowired
	private ScheduleRepository scheduleRepository;
	@Autowired
	private ViUserScheduleRepository viUserScheduleRepository;
	@Autowired
	private TimeCodeRepository timeCodeRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ValidationMessageRepository validationMessageRepository;
	@Autowired
	private CoreHourRepository coreHourRepository;
	@Autowired
	private ValidationMessageTextRepository validationMessageTextRepository;
	private ZoneId serverZoneId = ZoneId.of("America/New_York");

	@Autowired
	private ScheduleService scheduleService;

		@Autowired
	private RequestRepository requestRepository;

	@Autowired
	private SessionUserEmail UserEmail;

	@Autowired
	private AuditService auditService;

	@DeleteMapping
	public GeneralResponse deleteSchedules(@RequestBody List<Schedule> schedules) {
		GeneralResponse response = new GeneralResponse();
		List<Schedule> deletedSchedules = new ArrayList<Schedule>();
		List<String> errorMessages = new ArrayList<String>();

		try {
			for (Schedule schedule : schedules) {
				try {
					scheduleRepository.delete(schedule);
					deletedSchedules.add(schedule);
				} catch (Exception ex) {
					errorMessages.add(ex.getMessage());
				}
			}
			response.Status = "Success";
			response.Message = "Successfully deleted schedule(s)";
			response.Subject = deletedSchedules;
			if (errorMessages.size() > 0) {
				response.Message = String.format("%d schedules saved, %d schedules failed to save:\n%s",
						deletedSchedules.size(), errorMessages.size(), String.join("\n", errorMessages));
			}

			if (deletedSchedules.size() < 1) {
				throw new Exception("No schedule(s) were deleted from the database");
			}
			logger.info("deleted schedules of size {} successfully ", deletedSchedules.size());
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	@GetMapping("/scheduleCache")
	public GeneralResponse scheduleCache() {
		GeneralResponse response = new GeneralResponse();
		try {
			int currentYear = LocalDateTime.now().getYear();
			int lastYear = currentYear - 1;
			int nextYear = currentYear + 1;

			List<Schedule> schedules = scheduleRepository.findSchedulesByYearRange(lastYear, currentYear, nextYear);

			response.Status = "Success";
			response.Message = "Successfully retrieved user schedule cache";
			response.Subject = schedules;
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	@GetMapping("/timecodes")
	public GeneralResponse getTimeCodes() {
		GeneralResponse response = new GeneralResponse();
		try {

			response.Status = "Success";
			response.Message = "Successfully retrieved time codes";
			response.Subject = timeCodeRepository.findAllByOrderByTimeCodeValueAsc();
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	@PostMapping
	public GeneralResponse saveSchedules(@RequestBody List<Schedule> schedules) {
		GeneralResponse response = new GeneralResponse();
		List<Schedule> savedSchedules = new ArrayList<Schedule>();
		List<String> errorMessages = new ArrayList<String>();

		try {
			for (Schedule schedule : schedules) {
				try {
					Schedule sc = scheduleRepository.save(schedule);
					savedSchedules.add(sc);
				} catch (Exception ex) {
					errorMessages.add(ex.getMessage());
				}
			}
			response.Status = "Success";
			response.Message = "Successfully saved schedule(s)";
			response.Subject = savedSchedules;
			logger.info("saved schedules of size : {} successfully ", savedSchedules.size());
			if (errorMessages.size() > 0) {
				response.Message = String.format("%d schedules saved, %d schedules failed to save:\n%s",
						savedSchedules.size(), errorMessages.size(), String.join("\n", errorMessages));
			}

			if (savedSchedules.size() < 1) {
				throw new Exception("No schedule(s) were saved in the database");
			}
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	@GetMapping("/range")
	public GeneralResponse getSchedulesByRange(@RequestParam("startDate") LocalDate startDateParam,
			@RequestParam("endDate") LocalDate endDateParam) {
		GeneralResponse response = new GeneralResponse();
		try {
			OffsetDateTime startDate = startDateParam.atStartOfDay().atOffset(ZoneOffset.UTC);
			OffsetDateTime endDate = endDateParam.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);
			response.Status = "Success";
			response.Message = "Successfully retrieved user schedules";
			response.Subject = viUserScheduleRepository.findUserSchedulesBetweenDates(startDate, endDate);
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	@GetMapping("/{anchorDate}")
	public GeneralResponse getIndex(@PathVariable LocalDate anchorDate) {
		GeneralResponse response = new GeneralResponse();
		String netId = "Unknown"; // Assuming you manage user session in a similar way

		try {
			List<ViUserSchedule> userSchedules = viUserScheduleRepository.findByYearAndMonth(anchorDate.getYear(),
					anchorDate.getMonthValue());
			response.Status = "Success";
			response.Message = "Successfully retrieved user schedules";
			response.Subject = userSchedules;
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	@PostMapping("/validation/{netId}")
	public GeneralResponse validateSchedules(HttpServletRequest httpServletRequest,
			@PathVariable String netId,
			@RequestBody List<ValidationMessage> userMonths) {
		GeneralResponse response = new GeneralResponse();
		// Retrieve NetId from session if available
		// Object netIdObj = httpServletRequest.getSession().getAttribute("NetId");
		// if (netIdObj != null) {
		// 	netId = (String) netIdObj;
		// }
		List<ValidationMessage> validationMessages = new ArrayList<ValidationMessage>();
		List<ValidationMessagePlus> savedValidationMessages = new ArrayList<ValidationMessagePlus>();
		List<String> errorMessages = new ArrayList<String>();

		try {
			for (ValidationMessage um : userMonths) {
				User user = userRepository.findFirstByDempoidIgnoreCase(um.getDempoId());
				int schedulinglevel = user.getSchedulinglevel();
				if (schedulinglevel != 1 && schedulinglevel != 2 && schedulinglevel != 3) {
					deleteValidationMessages(um);
					continue;
				}
				CoreHour userCoreHours = coreHourRepository.findFirstByDempoid(um.getDempoId().toLowerCase());
				int currentCoreHours = getCoreHours(um.getInMonth(), userCoreHours);
				Map<Integer, String> vmtDictionary = new HashMap<Integer, String>();
				List<KeyValuePair> vmtext = validationMessageTextRepository.findMessageIdAndMessageText();
				for (KeyValuePair vmt : vmtext) {
					vmtDictionary.put(vmt.getKey(), vmt.getValue());
				}

				// get all schedules matching netid and year/month
				List<Schedule> schedules = scheduleRepository.findSchedulesByDempoIdAndYearMonth(um.getDempoId(),
						um.getInMonth().getYear(), um.getInMonth().getMonthValue());
				List<ViUserSchedule> schedulesWeekly = viUserScheduleRepository.findSchedulesByUserAndMonth(
						um.getDempoId(), um.getInMonth().getYear(), um.getInMonth().getMonthValue());

				int year = um.getInMonth().getYear();
				int month = um.getInMonth().getMonthValue();

				YearMonth yearMonth = YearMonth.of(year, month);
				int daysInMonth = yearMonth.lengthOfMonth();

				// Calculate the first day of the month
				LocalDate firstOfMonth = LocalDate.of(year, month, 1);

				// Get the day of the week for the first day of the month (1 = Monday, 7 =
				// Sunday)
				DayOfWeek dayOfWeek = firstOfMonth.getDayOfWeek();
				int firstDayOfMonth = dayOfWeek.getValue();

				int numOfWeeks = (firstDayOfMonth + daysInMonth) / 7;
				List<Schedule> schedulesTemp = new ArrayList<Schedule>();


				// ------------------------------------
				// all schedule levels
				// ------------------------------------
				if (schedulinglevel == 1 || schedulinglevel == 2 || schedulinglevel == 3) {
					// An Interviewer's weekly schedule should at a minimum match their core hours
					// total.
					List<LocalDateTime> schedulesWeeklyDistinct = schedulesWeekly.stream()
							.filter(s -> s.getStartdatetime() != null && s.getEnddatetime() != null)
							.map(ViUserSchedule::getWeekStart).distinct().sorted().collect(Collectors.toList());
					boolean hoursDontMatch = false;
					List<String> coreHoursDetails = new ArrayList<String>();
					List<String> coreHoursWeeks = new ArrayList<String>();

					for (LocalDateTime weekStart : schedulesWeeklyDistinct) {
						double totalHours = schedulesWeekly.stream()
								.filter(s -> s.getWeekStart().getYear() == weekStart.getYear()
										&& s.getWeekStart().getMonth() == weekStart.getMonth()
										&& s.getWeekStart().getDayOfMonth() == weekStart.getDayOfMonth()
										&& s.getStartdatetime() != null && s.getEnddatetime() != null
										&& s.getWeekStart() != null)
								.mapToDouble(s -> (s.getEnddatetime().atZoneSameInstant(serverZoneId).getHour()
										- s.getStartdatetime().atZoneSameInstant(serverZoneId).getHour())
										+ (s.getEnddatetime().atZoneSameInstant(serverZoneId).getMinute()
												- s.getStartdatetime().atZoneSameInstant(serverZoneId).getMinute()) / 60.0)
								.sum();
						logger.info("Total hours {} for week {} are : ", totalHours, weekStart);
						if (totalHours < currentCoreHours || totalHours > currentCoreHours) {
							if (coreHoursDetails.size() < 1) {
								coreHoursDetails.add(String.format("Core Hours: %.2f", (double) currentCoreHours));
							}

							hoursDontMatch = true;
							coreHoursDetails.add(String.format("Week of %s: %.2f hours",
									weekStart.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")), totalHours));
							coreHoursWeeks.add(weekStart.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
						}
					}

					if (hoursDontMatch) {
						addMessage(validationMessages, 7, um.getInMonth(), um.getDempoId(),
								String.join("|", coreHoursDetails) + "|||" + String.join("|", coreHoursWeeks));
					}
				}

				// ------------------------------------
				// schedule level 1/2 only
				// ------------------------------------
				if (schedulinglevel == 1 || schedulinglevel == 2) {
					// A shift schedule should be at least 4 hours in length.
					schedulesTemp = schedules.stream()
							.filter(s -> s.getStartdatetime() != null && s.getEnddatetime() != null)
							.filter(s -> Duration.between(s.getStartdatetime(), s.getEnddatetime())
									.toMinutes() < (4 * 60))
							.collect(Collectors.toList());

					addMessageByResult(validationMessages, schedulesTemp, 1, um.getInMonth(), um.getDempoId());
				}

				// ------------------------------------
				// schedule level 1 only
				// ------------------------------------
				if (schedulinglevel == 1) {
					// A shift schedule should be no more than 7 hours in length.
					schedulesTemp = schedules.stream()
							.filter(s -> s.getStartdatetime() != null && s.getEnddatetime() != null)
							.filter(s -> Duration.between(s.getStartdatetime(), s.getEnddatetime())
									.toMinutes() > (7 * 60))
							.collect(Collectors.toList());

					addMessageByResult(validationMessages, schedulesTemp, 2, um.getInMonth(), um.getDempoId());
				}

				// ------------------------------------
				// schedule level 2/3 only
				// ------------------------------------
				if (schedulinglevel == 2 || schedulinglevel == 3) {
					// A shift schedule cannot be exactly 8 hours in length.
					schedulesTemp = schedules.stream()
							.filter(s -> s.getStartdatetime() != null && s.getEnddatetime() != null)
							.filter(s -> Duration.between(s.getStartdatetime(), s.getEnddatetime())
									.toMinutes() == (8 * 60))
							.collect(Collectors.toList());

					addMessageByResult(validationMessages, schedulesTemp, 3, um.getInMonth(), um.getDempoId());
				}
				// ------------------------------------
				// schedule level 1 only
				// ------------------------------------
				if (schedulinglevel == 1) {
					// A shift schedule for a weekday, Monday thru Friday, should begin at or after
					// 1 PM.
					schedulesTemp = schedules.stream().filter(s -> s.getStartdatetime() != null)
							.filter(s -> s.getStartdatetime().atZoneSameInstant(serverZoneId).getDayOfWeek() != DayOfWeek.SATURDAY
									&& s.getStartdatetime().atZoneSameInstant(serverZoneId).getDayOfWeek() != DayOfWeek.SUNDAY
									&& s.getStartdatetime().atZoneSameInstant(serverZoneId).getHour() < 13)
							.collect(Collectors.toList());

					addMessageByResult(validationMessages, schedulesTemp, 4, um.getInMonth(), um.getDempoId());

					// A shift schedule for Saturday should begin at or after 9 AM.
					schedulesTemp = schedules.stream().filter(s -> s.getStartdatetime() != null)
							.filter(s -> s.getStartdatetime().atZoneSameInstant(serverZoneId).getDayOfWeek() == DayOfWeek.SATURDAY
									&& s.getStartdatetime().atZoneSameInstant(serverZoneId).getHour() < 9)
							.collect(Collectors.toList());

					addMessageByResult(validationMessages, schedulesTemp, 5, um.getInMonth(), um.getDempoId());

					// A shift schedule for Sunday should begin at or after 12 noon.
					schedulesTemp = schedules.stream().filter(s -> s.getStartdatetime() != null)
							.filter(s -> s.getStartdatetime().atZoneSameInstant(serverZoneId).getDayOfWeek() == DayOfWeek.SUNDAY
									&& s.getStartdatetime().atZoneSameInstant(serverZoneId).getHour() < 12)
							.collect(Collectors.toList());

					addMessageByResult(validationMessages, schedulesTemp, 6, um.getInMonth(), um.getDempoId());
				}
				
				List<LocalDateTime> schedulesWeeklyDistinct = schedulesWeekly.stream()
						.filter(s -> s.getStartdatetime() != null && s.getEnddatetime() != null)
						.map(ViUserSchedule::getWeekStart).distinct().sorted().collect(Collectors.toList());
				List<String> coreHoursDetails = new ArrayList<String>();
				List<String> coreHoursWeeks = new ArrayList<String>();

				boolean greaterThan20 = false;
				List<String> greaterThan20Details = new ArrayList<String>();
				boolean greaterThan40 = false;
				List<String> greaterThan40Details = new ArrayList<String>();
				List<String> greaterThan40Weeks = new ArrayList<String>();

				for (LocalDateTime weekStart : schedulesWeeklyDistinct) {
					double totalHours = schedulesWeekly.stream()
							.filter(s -> s.getWeekStart().getYear() == weekStart.getYear()
									&& s.getWeekStart().getMonth() == weekStart.getMonth()
									&& s.getWeekStart().getDayOfMonth() == weekStart.getDayOfMonth()
									&& s.getStartdatetime() != null && s.getEnddatetime() != null
									&& s.getWeekStart() != null)
							.mapToDouble(s -> (s.getEnddatetime().atZoneSameInstant(serverZoneId).getHour()
									- s.getStartdatetime().atZoneSameInstant(serverZoneId).getHour())
									+ (s.getEnddatetime().atZoneSameInstant(serverZoneId).getMinute()
											- s.getStartdatetime().atZoneSameInstant(serverZoneId).getMinute()) / 60.0)
							.sum();
					logger.info("Total hours {} for week {} are : ", totalHours, weekStart);

					// An Interviewer's weekly schedule should not exceed 20 hours total.
					if (totalHours > 20) {
						greaterThan20 = true;
						greaterThan20Details.add(String.format("Week of %s: %.2f hours",
								weekStart.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")), totalHours));
					}
					
					// An Interviewer's weekly schedule should not exceed 40 hours total.
					if (totalHours > 40) {
						greaterThan40 = true;
						greaterThan40Details.add(String.format("Week of %s: %.2f hours",
								weekStart.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")), totalHours));
						greaterThan40Weeks.add(weekStart.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
					}
				}

				// ------------------------------------
				// schedule level 1 only
				// ------------------------------------
				// if (schedulinglevel == 1) {
				// 	// An Interviewer's weekly schedule should not exceed 20 hours total.
				// 	if (greaterThan20) {
				// 		addMessage(validationMessages, 8, um.getInMonth(), um.getDempoId(),
				// 				String.join("|", greaterThan20Details));
				// 	}
				// }

				// ------------------------------------
				// schedule level 2/3 only
				// ------------------------------------
				if (schedulinglevel == 2 || schedulinglevel == 3) { 
					// An Interviewer's weekly schedule should not exceed 40 hours total.
					if (greaterThan40) {
						addMessage(validationMessages, 9, um.getInMonth(), um.getDempoId(),
								String.join("|", greaterThan40Details) + "|||" + String.join("|", greaterThan40Weeks));
					}
				}

				// ------------------------------------
				// schedule level 1 only
				// ------------------------------------
				// An interviewer's schedule should include 2 night shifts, until at or after 9 p.m., each month
				if (schedulinglevel == 1) {
					List<LocalDateTime> schedulesWeeklyTemp = schedulesWeekly.stream()
							.filter(s -> s.getStartdatetime() != null && s.getEnddatetime() != null
									&& (s.getEnddatetime().atZoneSameInstant(serverZoneId).getHour() >= 21
											|| s.getStartdatetime().atZoneSameInstant(serverZoneId).getHour() >= 21))
							.map(ViUserSchedule::getWeekStart).distinct().sorted().collect(Collectors.toList());

					if (schedulesWeeklyTemp.size() < 2) {
						addMessage(validationMessages, 10, um.getInMonth(), um.getDempoId(), null);
					}
				}

				// ------------------------------------
				// schedule level 1/2 only
				// ------------------------------------
				if (schedulinglevel == 1 || schedulinglevel == 2) {

					//TODO: change to 2 weekend shifts each month

					//WAS:

					// An Interviewer's schedule should include 1 weekend shift every other week.
					// A Friday night shift schedule with majority of hours after 5 PM, can only
					// have 1 Friday night per month.
					// A Saturday and / or Sunday shift schedule should be 6 hours minimum.

					// NOW:
					// An Interviewer's schedule should include 2 weekend shifts each month.
					// A Saturday or Sunday shift schedule should be 4 hours minimum.
					

					// get week starts having: A Saturday and / or Sunday shift schedule should be 6
					// hours minimum.
					// Get all weekend shifts that are at least 4 hours long
					List<OffsetDateTime> weekendShifts = schedulesWeekly.stream()
							.filter(s -> s.getStartdatetime() != null && s.getEnddatetime() != null
									&& (s.getStartdatetime().atZoneSameInstant(serverZoneId).getDayOfWeek() == DayOfWeek.SATURDAY
											|| s.getStartdatetime().atZoneSameInstant(serverZoneId).getDayOfWeek() == DayOfWeek.SUNDAY)
							)
							.map(ViUserSchedule::getStartdatetime)
							.distinct()
							.collect(Collectors.toList());

					// Check if there are at least 2 weekend shifts in the month
					if (weekendShifts.size() < 2) {
						addMessage(validationMessages, 11, um.getInMonth(), um.getDempoId(), null);
					}
				}

				// ------------------------------------
				// end validation lines
				// ------------------------------------

				if (validationMessages.size() > 0) {
					// insert validation messages
					try {
						deleteValidationMessages(um);
						// now write message entries using the new id
						for (ValidationMessage vm : validationMessages) {
							// even if we have one from before, we drop it so we set the validationmessageid
							// to 0
							vm.setValidationMessagesId(0);
							vm = validationMessageRepository.save(vm);
							String messageText = "";
							if (vmtDictionary.containsKey(vm.getMessageId())) {
								messageText = vmtDictionary.get(vm.getMessageId());
							}

							savedValidationMessages.add(new ValidationMessagePlus(vm, messageText));
						}
					} catch (Exception ex) {
						errorMessages.add(ex.getMessage());
					}
				} // if no validation messages, drop/delete any validation messages in the
					// database that match the netid (dempoid) and date
				else {
					deleteValidationMessages(um);
				}

			}
			response.Status = "Success";
			response.Message = "Successfully saved validation message(s)";
			response.Subject = savedValidationMessages;

			if (errorMessages.size() > 0) {

				response.Message = String.format(
						"%d validation messages saved, %d validation messages failed to save:\n%s",
						savedValidationMessages.size(), errorMessages.size(), String.join("\n", errorMessages));

			}
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}


	private void deleteValidationMessages(ValidationMessage userMonth) {
		try {
			List<ValidationMessage> existingValidationMessages = validationMessageRepository
					.findByDempoIdAndInMonthYearAndInMonthMonth(userMonth.getDempoId(), userMonth.getInMonth().getYear(),
							userMonth.getInMonth().getMonthValue());
			if (existingValidationMessages.size() > 0) {
				validationMessageRepository.deleteAll(existingValidationMessages);
			}
		} catch (Exception ex) {
			// If deletion fails due to concurrent modification, log and continue
			// The next save operation will handle the conflict
			logger.warn("Concurrent deletion detected for validation messages. Will retry on save.", ex);
		}
	}

	private Integer getCoreHours(LocalDate inMonth, CoreHour userCoreHours) {
		inMonth = LocalDate.of(inMonth.getYear(), inMonth.getMonth(), 1);

		if (inMonth.equals(userCoreHours.getMonth1())) {
			return userCoreHours.getCoreHours1();
		}
		if (inMonth.equals(userCoreHours.getMonth2())) {
			return userCoreHours.getCoreHours2();
		}
		if (inMonth.equals(userCoreHours.getMonth3())) {
			return userCoreHours.getCoreHours3();
		}
		if (inMonth.equals(userCoreHours.getMonth4())) {
			return userCoreHours.getCoreHours4();
		}
		if (inMonth.equals(userCoreHours.getMonth5())) {
			return userCoreHours.getCoreHours5();
		}
		if (inMonth.equals(userCoreHours.getMonth6())) {
			return userCoreHours.getCoreHours6();
		}
		if (inMonth.equals(userCoreHours.getMonth7())) {
			return userCoreHours.getCoreHours7();
		}
		if (inMonth.equals(userCoreHours.getMonth8())) {
			return userCoreHours.getCoreHours8();
		}
		if (inMonth.equals(userCoreHours.getMonth9())) {
			return userCoreHours.getCoreHours9();
		}
		if (inMonth.equals(userCoreHours.getMonth10())) {
			return userCoreHours.getCoreHours10();
		}
		if (inMonth.equals(userCoreHours.getMonth11())) {
			return userCoreHours.getCoreHours11();
		}
		if (inMonth.equals(userCoreHours.getMonth12())) {
			return userCoreHours.getCoreHours12();
		}
		if (inMonth.equals(userCoreHours.getMonth13())) {
			return userCoreHours.getCoreHours13();
		}
		if (inMonth.equals(userCoreHours.getMonth14())) {
			return userCoreHours.getCoreHours14();
		}

		return 0;
	}

	public void addMessageByResult(List<ValidationMessage> validationMessages, List<Schedule> schedules, int messageId,
			LocalDate inMonth, String dempoId) {
		if (schedules.size() > 0) {
			Collections.sort(schedules, (s1, s2) -> s1.getStartdatetime().compareTo(s2.getStartdatetime()));

			List<String> scheduleKeys = schedules.stream().map(schedule -> String.valueOf(schedule.getPreschedulekey()))
					.collect(Collectors.toList());

			validationMessages.add(new ValidationMessage(messageId, inMonth, dempoId, String.join("|", scheduleKeys)));
		}
	}

	private void addMessage(List<ValidationMessage> validationMessage, int messageId, LocalDate inMonth, String netId,
			String details) {
		validationMessage.add(new ValidationMessage(netId, messageId, inMonth, details));
	}

	@GetMapping("/validationMessages/{inDate}")
	public GeneralResponse getValidationMessages(@PathVariable LocalDate inDate, @RequestParam String netId) {
		GeneralResponse response = new GeneralResponse();
		try {
			List<Object[]> rawResults;
			if (!isNullOrWhiteSpace(netId)) {
				rawResults = validationMessageRepository.findValidationMessagesByNetIdAndMonth(netId, inDate);
			} else {
				rawResults = validationMessageRepository.findValidationMessagesByInDate(inDate);
			}
			List<ValidationMessagePlus> validationMessages = new ArrayList<>();
			for (Object[] row : rawResults) {
				ValidationMessagePlus vmp = new ValidationMessagePlus();
				vmp.setValidationMessagesId(row[0] instanceof Integer ? (Integer) row[0] : ((Number) row[0]).intValue());
				vmp.setDempoId((String) row[1]);
				vmp.setMessageId(row[2] instanceof Integer ? (Integer) row[2] : ((Number) row[2]).intValue());
				if (row[3] instanceof java.sql.Date) {
					vmp.setInMonth(((java.sql.Date) row[3]).toLocalDate());
				} else if (row[3] instanceof java.time.LocalDate) {
					vmp.setInMonth((java.time.LocalDate) row[3]);
				}
				vmp.setScheduleKeys((String) row[4]);
				vmp.setDetails((String) row[5]);
				vmp.setMessageText((String) row[6]);
				validationMessages.add(vmp);
			}
			List<Integer> valMessageIds = validationMessages.stream()
					.map(ValidationMessagePlus::getValidationMessagesId).collect(Collectors.toList());
			List<ValidationMessage> vmTemp = validationMessageRepository.findValidationMessagesByIds(valMessageIds);
			List<Integer> uniqueMessageIds = new ArrayList<Integer>();
			List<ValidationMessagePlus> uniqueValidationMessages = new ArrayList<ValidationMessagePlus>();
			// add relevant schedules and delete duplicates
			for (ValidationMessagePlus vmp : validationMessages) {
				// add relevant schedules
				if (!isNullOrWhiteSpace(vmp.getScheduleKeys())) {
					List<String> scheduleKeys = Arrays.asList(vmp.getScheduleKeys().split("\\|"));
					List<Schedule> schedules = scheduleRepository.findSchedulesByScheduleKeys(scheduleKeys);
					vmp.setSchedules(schedules);
				}
				// delete duplicates
				if (uniqueMessageIds.contains(vmp.getMessageId())) {
					// delete duplicate
					ValidationMessage vMessage = vmTemp.stream().filter(vm -> vm.getMessageId() == vmp.getMessageId())
							.findFirst().orElse(null);
					if (vMessage != null) {
						validationMessageRepository.delete(vMessage);
					}
				} else {
					// add to resulting validation messages to return
					uniqueValidationMessages.add(vmp);
					uniqueMessageIds.add(vmp.getMessageId());
				}
			}
			response.Status = "Success";
			response.Message = "Successfully retrieved validation messages";
			response.Subject = uniqueValidationMessages;
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	public static boolean isNullOrWhiteSpace(String str) {
		return str == null || str.trim().isEmpty();
	}

	@PostMapping("/save-schedule")
	public ResponseEntity<GeneralResponse> saveSchedule(@RequestBody List<ShiftScheduleRequest> request) {
		GeneralResponse response = scheduleService.saveSchedule(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/schedule-list/{anchorDate}")
	public ResponseEntity<List<ScheduleResponse>> getScheduleList(@PathVariable String anchorDate,
			@RequestParam(required = false, value = "demId") String dempoId,
			@RequestParam(required = false, value = "project_id") Integer projectId,
			@RequestParam(required = false, value = "schedule_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate scheduleDate,
			@RequestParam(required = false, value = "tab_value") String tabValue,
			@RequestParam(required = false, value = "start_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(required = false, value = "end_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
		LocalDate parsedAnchorDate = LocalDate.parse(anchorDate.substring(0, 10), DateTimeFormatter.ISO_DATE);
		System.out.println("dempoId---------" + dempoId);
		List<ScheduleResponse> response = scheduleService.getList(dempoId, projectId, scheduleDate, tabValue, startDate,
				endDate, parsedAnchorDate.getYear(), parsedAnchorDate.getMonthValue());

		if (response == null || response.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(Collections.emptyList());
		}

		return ResponseEntity.ok(response);
	}

	@PostMapping("/update-schedule")
	public ResponseEntity<GeneralResponse> updateSchedule(@RequestBody ShiftScheduleRequest request) {
		GeneralResponse response = scheduleService.updateSchedule(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@DeleteMapping("/delete-schedule/{id}/{netId}")
	public ResponseEntity<GeneralResponse> deleteSchedule(@PathVariable("id") Long id,@PathVariable("netId") String netId) {
		GeneralResponse response = scheduleService.deleteSchedule(id, netId);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@GetMapping("/option-value")
	public ResponseEntity<GeneralResponse> getOptionValue() {
		GeneralResponse response = scheduleService.getOptionValue();
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@GetMapping("/core-hours")
	public ResponseEntity<GeneralResponse> getCoreHours(
			@RequestParam(required = false, value = "dempoId") String dempoId,
			@RequestParam(required = false, value = "scheduleDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate scheduleDate) {
		GeneralResponse response = scheduleService.getCoreHours(scheduleDate, dempoId);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

}
