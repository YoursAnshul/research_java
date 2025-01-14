package com.proep.api.models.dataaccess.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proep.api.models.dataaccess.Training;

public interface TrainingRepository extends JpaRepository<Training, Integer> {
	 List<Training> findByDempoidIgnoreCase(String dempoid);

}
