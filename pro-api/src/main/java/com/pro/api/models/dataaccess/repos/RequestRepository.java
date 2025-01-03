package com.pro.api.models.dataaccess.repos;

import com.pro.api.models.dataaccess.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Integer> {
	List<Request> findAllByOrderByRequestDateDesc();

	@Query("SELECT r FROM Request r WHERE r.interviewerEmpId = :netId ORDER BY r.requestDate DESC")
	List<Request> findAllByInterviewerEmpIdOrderByRequestDateDesc(String netId);
}
