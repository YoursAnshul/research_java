package com.pro.api.models.dataaccess.repos;

import com.pro.api.models.dataaccess.TimeCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeCodeRepository extends JpaRepository<TimeCode, Integer> {
	List<TimeCode> findAllByOrderByTimeCodeValueAsc();
}
