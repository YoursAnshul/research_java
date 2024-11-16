package com.pro.api.models.dataaccess.repos;

import com.pro.api.models.dataaccess.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {

	List<Project> findByActive(int active);

	List<Project> findAllByOrderByProjectName();

	List<Project> findAllByActiveOrderByProjectName(int active);

	@Query("SELECT p.projectId FROM Project p WHERE p.active = 1 ORDER BY p.projectId")
	List<Integer> findSortedActiveProjectIds();
}
