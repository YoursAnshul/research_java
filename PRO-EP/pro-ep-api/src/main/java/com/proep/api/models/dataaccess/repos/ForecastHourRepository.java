package com.proep.api.models.dataaccess.repos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proep.api.models.dataaccess.ForecastHour;

public interface ForecastHourRepository extends JpaRepository<ForecastHour, Integer> {
	ForecastHour findFirstByProjectId(int projectId);

}
