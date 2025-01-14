package com.proep.api.models.dataaccess.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.proep.api.models.dataaccess.Request;

public interface RequestRepository extends JpaRepository<Request, Integer> {
	List<Request> findAllByOrderByRequestDateDesc();

	@Query("SELECT r FROM Request r WHERE r.interviewerEmpId = :netId ORDER BY r.requestDate DESC")
	List<Request> findAllByInterviewerEmpIdOrderByRequestDateDesc(String netId);
}
