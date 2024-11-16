package com.pro.api.models.dataaccess.repos;

import com.pro.api.models.dataaccess.Training;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainingRepository extends JpaRepository<Training, Integer> {
	 List<Training> findByDempoidIgnoreCase(String dempoid);

}
