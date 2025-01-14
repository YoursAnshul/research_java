package com.proep.api.models.dataaccess.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proep.api.models.dataaccess.TimeCode;

@Repository
public interface TimeCodeRepository extends JpaRepository<TimeCode, Integer> {
	List<TimeCode> findAllByOrderByTimeCodeValueAsc();

}
