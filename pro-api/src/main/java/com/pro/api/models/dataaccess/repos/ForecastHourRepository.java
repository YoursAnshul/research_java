package com.pro.api.models.dataaccess.repos;

import com.pro.api.models.dataaccess.ForecastHour;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForecastHourRepository extends JpaRepository<ForecastHour, Integer> {
	ForecastHour findFirstByProjectId(int projectId);

}
