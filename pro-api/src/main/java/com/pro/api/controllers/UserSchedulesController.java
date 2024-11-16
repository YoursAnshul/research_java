package com.pro.api.controllers;

import com.pro.api.models.dataaccess.Schedule;
import com.pro.api.models.dataaccess.ViUserSchedule;
import com.pro.api.models.dataaccess.repos.ScheduleRepository;
import com.pro.api.models.dataaccess.repos.TimeCodeRepository;
import com.pro.api.models.dataaccess.repos.ViUserScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

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
            logger.info("deleted schedules of size {} successfully ",deletedSchedules.size());
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
            logger.info("saved schedules of size : {} successfully ",savedSchedules.size());
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

}
