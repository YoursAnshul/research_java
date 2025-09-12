package com.pro.api.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pro.api.controllers.GeneralResponse;
import com.pro.api.models.dataaccess.AdminOption;
import com.pro.api.models.dataaccess.CoreHour;
import com.pro.api.models.dataaccess.ViUserSchedule;
import com.pro.api.models.dataaccess.repos.AdminOptionRepository;
import com.pro.api.models.dataaccess.repos.CoreHourRepository;
import com.pro.api.models.dataaccess.repos.ScheduleRepository;
import com.pro.api.models.dataaccess.repos.ViUserScheduleRepository;
import com.pro.api.response.ShiftScheduleRequest;

@Service
@Transactional
public class ScheduleService {
    @Autowired
    private ViUserScheduleRepository viUserScheduleRepository;
    
    @Autowired
    private ScheduleRepository scheduleRepository;
    
    @Autowired
    private AdminOptionRepository adminOptionRepository;
    
    @Autowired
    private CoreHourRepository coreHourRepository;
    

    public List<ViUserSchedule> getList(String dempoId, Integer projectId, LocalDate scheduleDate, 
            String tabValue, LocalDate startDate, LocalDate endDate, int year, int month) {
        return viUserScheduleRepository.findSchedules(dempoId, projectId, year, month);
    }


    public GeneralResponse getOptionValue() {
        AdminOption obj = adminOptionRepository.findByAdminOptionsId(9);
        GeneralResponse res = new GeneralResponse();
        res.Subject = obj;
        return res;
    }

    public GeneralResponse getCoreHours(LocalDate scheduleDate, String dempoId) {
        int month = scheduleDate.getMonthValue();
        int year = scheduleDate.getYear();

        CoreHour coreHour = coreHourRepository.findFirstByDempoidAndMonthYear(dempoId, month, year);
        GeneralResponse response = new GeneralResponse();
        Integer coreHoursValue = 0;

        if (coreHour == null) {
            response.Subject = coreHoursValue;
            return response;
        }

        java.time.YearMonth target = java.time.YearMonth.from(scheduleDate);

        if (coreHour.getMonth1() != null && java.time.YearMonth.from(coreHour.getMonth1()).equals(target)) {
            coreHoursValue = coreHour.getCoreHours1();
        } else if (coreHour.getMonth2() != null && java.time.YearMonth.from(coreHour.getMonth2()).equals(target)) {
            coreHoursValue = coreHour.getCoreHours2();
        } else if (coreHour.getMonth3() != null && java.time.YearMonth.from(coreHour.getMonth3()).equals(target)) {
            coreHoursValue = coreHour.getCoreHours3();
        } else if (coreHour.getMonth4() != null && java.time.YearMonth.from(coreHour.getMonth4()).equals(target)) {
            coreHoursValue = coreHour.getCoreHours4();
        } else if (coreHour.getMonth5() != null && java.time.YearMonth.from(coreHour.getMonth5()).equals(target)) {
            coreHoursValue = coreHour.getCoreHours5();
        } else if (coreHour.getMonth6() != null && java.time.YearMonth.from(coreHour.getMonth6()).equals(target)) {
            coreHoursValue = coreHour.getCoreHours6();
        } else if (coreHour.getMonth7() != null && java.time.YearMonth.from(coreHour.getMonth7()).equals(target)) {
            coreHoursValue = coreHour.getCoreHours7();
        } else if (coreHour.getMonth8() != null && java.time.YearMonth.from(coreHour.getMonth8()).equals(target)) {
            coreHoursValue = coreHour.getCoreHours8();
        } else if (coreHour.getMonth9() != null && java.time.YearMonth.from(coreHour.getMonth9()).equals(target)) {
            coreHoursValue = coreHour.getCoreHours9();
        } else if (coreHour.getMonth10() != null && java.time.YearMonth.from(coreHour.getMonth10()).equals(target)) {
            coreHoursValue = coreHour.getCoreHours10();
        } else if (coreHour.getMonth11() != null && java.time.YearMonth.from(coreHour.getMonth11()).equals(target)) {
            coreHoursValue = coreHour.getCoreHours11();
        } else if (coreHour.getMonth12() != null && java.time.YearMonth.from(coreHour.getMonth12()).equals(target)) {
            coreHoursValue = coreHour.getCoreHours12();
        } else if (coreHour.getMonth13() != null && java.time.YearMonth.from(coreHour.getMonth13()).equals(target)) {
            coreHoursValue = coreHour.getCoreHours13();
        } else if (coreHour.getMonth14() != null && java.time.YearMonth.from(coreHour.getMonth14()).equals(target)) {
            coreHoursValue = coreHour.getCoreHours14();
        }

        response.Subject = coreHoursValue;
        return response;
    }

    // Note: These methods need to be implemented based on your business requirements
    public GeneralResponse saveSchedule(List<ShiftScheduleRequest> request) {
        throw new UnsupportedOperationException("Method needs to be implemented using JPA");
    }

    public GeneralResponse updateSchedule(ShiftScheduleRequest request) {
        throw new UnsupportedOperationException("Method needs to be implemented using JPA");
    }

    public GeneralResponse deleteSchedule(Long id, String netId) {
        GeneralResponse response = new GeneralResponse();
        
        try {
            // Check if the schedule exists
            if (scheduleRepository.existsById(id.intValue())) {
                scheduleRepository.deleteById(id.intValue());
                response.Status = "Success";
                response.Message = "Schedule deleted successfully";
            } else {
                response.Status = "Failure";
                response.Message = "Schedule with ID " + id + " not found";
            }
        } catch (Exception ex) {
            response.Status = "Failure";
            response.Message = "Error deleting schedule: " + ex.getMessage();
        }
        
        return response;
    }
}
