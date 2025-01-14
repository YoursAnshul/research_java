package com.proep.api.models.dataaccess.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proep.api.models.dataaccess.Log;

@Repository
public interface LogRepository extends JpaRepository<Log, Integer> {

	
}
