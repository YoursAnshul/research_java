package com.pro.api.models.dataaccess.repos;

import com.pro.api.models.dataaccess.InterviewerTimeCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewerTimeCardRepository extends JpaRepository<InterviewerTimeCard, Integer> {

	 List<InterviewerTimeCard> findByDempoidIgnoreCaseOrderByDatetimeinDesc(String dempoid);


}
