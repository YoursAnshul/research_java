package com.proep.api.models.dataaccess.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proep.api.models.dataaccess.InterviewerTimeCard;

@Repository
public interface InterviewerTimeCardRepository extends JpaRepository<InterviewerTimeCard, Integer> {

	 List<InterviewerTimeCard> findByDempoidIgnoreCaseOrderByDatetimeinDesc(String dempoid);


}
